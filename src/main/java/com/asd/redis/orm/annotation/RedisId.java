package com.asd.redis.orm.annotation;

import java.lang.annotation.*;

/**
 * 标记字段为Redis实体的ID
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisId {
    /**
     * ID生成策略
     */
    IdType type() default IdType.AUTO;

    /**
     * ID生成策略枚举
     */
    enum IdType {
        /**
         * 自动生成UUID
         */
        UUID,

        /**
         * 自动递增
         */
        AUTO,

        /**
         * 手动指定
         */
        INPUT
    }
}