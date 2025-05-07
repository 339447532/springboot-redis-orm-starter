package com.asd.redis.orm.spring;

import com.asd.redis.orm.mapper.BaseMapperFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.TypeFilter;

import java.util.Arrays;
import java.util.Set;

/**
 * Redis Mapper类路径扫描器
 */
public class ClassPathRedisMapperScanner extends ClassPathBeanDefinitionScanner {

    private Class<?> markerInterface;

    public ClassPathRedisMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public void registerFilters() {
        // 默认包含所有非接口和非抽象类
        addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(org.springframework.core.type.classreading.MetadataReader metadataReader,
                                 org.springframework.core.type.classreading.MetadataReaderFactory metadataReaderFactory) {
                return true;
            }
        });

        // 排除package-info.java
        addExcludeFilter(new TypeFilter() {
            @Override
            public boolean match(org.springframework.core.type.classreading.MetadataReader metadataReader,
                                 org.springframework.core.type.classreading.MetadataReaderFactory metadataReaderFactory) {
                String className = metadataReader.getClassMetadata().getClassName();
                return className.endsWith("package-info");
            }
        });
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            logger.warn("No Redis Mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;

        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            String beanClassName = definition.getBeanClassName();

            // 设置构造函数参数为接口类
            definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);

            // 将bean类型改为FactoryBean
            definition.setBeanClass(BaseMapperFactoryBean.class);

            definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
        }
    }
}