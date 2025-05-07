package com.asd.redis.orm.annotation;

import com.asd.redis.orm.spring.RedisMapperScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Redis Mapper扫描注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RedisMapperScannerRegistrar.class)
public @interface RedisMapperScan {

    /**
     * 扫描的包路径
     */
    String[] value() default {};

    /**
     * 扫描的包路径
     */
    String[] basePackages() default {};

    /**
     * 扫描的包类
     */
    Class<?>[] basePackageClasses() default {};
}