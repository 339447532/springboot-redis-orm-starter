package com.asd.redis.orm.annotation;

import java.lang.annotation.*;

/**
 * 标记接口为Redis Mapper
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisMapper {
    /**
     * 映射的实体类
     */
    Class<?> entity();
}