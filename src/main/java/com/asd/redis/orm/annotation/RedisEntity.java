package com.asd.redis.orm.annotation;

import java.lang.annotation.*;

/**
 * 标记实体类为Redis实体
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisEntity {
    /**
     * Redis键前缀
     */
    String prefix() default "";

    /**
     * 过期时间（秒），默认-1表示永不过期
     */
    long expire() default -1;
}