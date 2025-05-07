package com.asd.redis.orm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis ORM配置属性
 */
@ConfigurationProperties(prefix = "redis.orm")
public class RedisOrmProperties {

    /**
     * 键前缀
     */
    private String keyPrefix = "";

    /**
     * 默认过期时间（秒），-1表示永不过期
     */
    private long defaultExpireTime = -1;

    /**
     * 是否启用缓存
     */
    private boolean enableCache = true;

    /**
     * 缓存大小
     */
    private int cacheSize = 1000;

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public long getDefaultExpireTime() {
        return defaultExpireTime;
    }

    public void setDefaultExpireTime(long defaultExpireTime) {
        this.defaultExpireTime = defaultExpireTime;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
}