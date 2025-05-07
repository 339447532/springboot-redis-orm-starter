package com.asd.redis.orm.mapper;

import com.asd.redis.orm.core.RedisOrmTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BaseMapper工厂Bean，用于创建Mapper接口的代理实现
 */
public class BaseMapperFactoryBean<T> implements FactoryBean<T> {

    private Class<T> mapperInterface;

    @Autowired
    private RedisOrmTemplate redisOrmTemplate;

    public BaseMapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() throws Exception {
        BaseMapperProxy<T> mapperProxy = new BaseMapperProxy<>(redisOrmTemplate, mapperInterface);
        return mapperProxy.getProxy();
    }

    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}