package com.asd.redis.orm.spring;

import com.asd.redis.orm.annotation.RedisMapper;
import com.asd.redis.orm.annotation.RedisMapperScan;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis Mapper扫描注册器
 */
public class RedisMapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(RedisMapperScan.class.getName()));

        if (mapperScanAttrs != null) {
            registerBeanDefinitions(mapperScanAttrs, registry);
        }
    }

    private void registerBeanDefinitions(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry) {
        ClassPathRedisMapperScanner scanner = new ClassPathRedisMapperScanner(registry);

        // 添加过滤器，只扫描带有@RedisMapper注解的接口
        scanner.addIncludeFilter(new AnnotationTypeFilter(RedisMapper.class));

        scanner.setResourceLoader(this.resourceLoader);

        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(
                Arrays.stream(annoAttrs.getStringArray("value"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList()));

        basePackages.addAll(
                Arrays.stream(annoAttrs.getStringArray("basePackages"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList()));

        basePackages.addAll(
                Arrays.stream(annoAttrs.getClassArray("basePackageClasses"))
                        .map(ClassUtils::getPackageName)
                        .collect(Collectors.toList()));

        // 如果没有指定包，则使用当前类所在的包
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(annoAttrs.getClass("value")));
        }

        scanner.registerFilters();
        scanner.doScan(StringUtils.toStringArray(basePackages));
    }
}