package com.asd.redis.orm.annotation;

import java.lang.annotation.*;

/**
 * 标记字段为Redis实体的属性
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisField {
    /**
     * 字段名称，默认使用属性名
     */
    String name() default "";

    /**
     * 是否忽略该字段
     */
    boolean ignore() default false;
}