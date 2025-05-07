package com.asd.redis.orm.mapper;

import com.asd.redis.orm.core.RedisOrmTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * BaseMapper代理类，用于生成Mapper接口的动态代理
 */
public class BaseMapperProxy<T> implements InvocationHandler {

    private final RedisOrmTemplate redisOrmTemplate;
    private final Class<T> mapperInterface;
    private final BaseMapperImpl<Object> baseMapper;

    @SuppressWarnings("unchecked")
    public BaseMapperProxy(RedisOrmTemplate redisOrmTemplate, Class<T> mapperInterface) {
        this.redisOrmTemplate = redisOrmTemplate;
        this.mapperInterface = mapperInterface;
        this.baseMapper = new BaseMapperImpl<>(redisOrmTemplate, mapperInterface);
    }

    @SuppressWarnings("unchecked")
    public T getProxy() {
        return (T) Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class[]{mapperInterface},
                this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 如果是Object类的方法，直接调用
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }

        // 调用BaseMapperImpl中对应的方法
        Method baseMapperMethod = BaseMapper.class.getMethod(method.getName(), method.getParameterTypes());
        return baseMapperMethod.invoke(baseMapper, args);
    }
}