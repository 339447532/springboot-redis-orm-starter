package com.asd.redis.orm.mapper;

import com.asd.redis.orm.annotation.RedisMapper;
import com.asd.redis.orm.core.RedisOrmTemplate;
import com.asd.redis.orm.model.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 基础Mapper实现类
 */
public class BaseMapperImpl<T> implements BaseMapper<T> {

    private final RedisOrmTemplate redisOrmTemplate;
    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public BaseMapperImpl(RedisOrmTemplate redisOrmTemplate, Class<?> mapperClass) {
        this.redisOrmTemplate = redisOrmTemplate;

        RedisMapper annotation = mapperClass.getAnnotation(RedisMapper.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Mapper class must be annotated with @RedisMapper");
        }

        this.entityClass = (Class<T>) annotation.entity();
    }

    @Override
    public T insert(T entity) {
        return redisOrmTemplate.save(entity);
    }

    @Override
    public List<T> insertBatch(Collection<T> entityList) {
        return redisOrmTemplate.saveBatch(entityList);
    }

    @Override
    public boolean deleteById(Serializable id) {
        return redisOrmTemplate.removeById(entityClass, id);
    }

    @Override
    public boolean deleteBatchIds(Collection<? extends Serializable> idList) {
        return redisOrmTemplate.removeByIds(entityClass, idList);
    }

    @Override
    public boolean updateById(T entity) {
        return redisOrmTemplate.updateById(entity);
    }

    @Override
    public boolean updateBatchById(Collection<T> entityList) {
        return redisOrmTemplate.updateBatchById(entityList);
    }

    @Override
    public T selectById(Serializable id) {
        return redisOrmTemplate.getById(entityClass, id);
    }

    @Override
    public List<T> selectBatchIds(Collection<? extends Serializable> idList) {
        return redisOrmTemplate.listByIds(entityClass, idList);
    }

    @Override
    public Page<T> selectPage(long current, long size) {
        return redisOrmTemplate.page(entityClass, current, size);
    }

    @Override
    public long selectCount() {
        return redisOrmTemplate.count(entityClass);
    }

    @Override
    public List<T> selectByCondition(T condition) {
        // 这里需要实现根据条件查询的逻辑
        if (condition == null) {
            // 如果条件为空，返回所有记录
            return redisOrmTemplate.list(entityClass);
        }
        // 通过反射获取条件对象的非空字段作为查询条件
        return redisOrmTemplate.listByCondition(entityClass, condition);
    }

    @Override
    public Page<T> selectPageByCondition(T condition, long current, long size) {
        // 实现根据条件的分页查询
        if (condition == null) {
            // 如果条件为空，返回普通分页结果
            return redisOrmTemplate.page(entityClass, current, size);
        }
        // 使用条件进行分页查询
        return redisOrmTemplate.pageByCondition(entityClass, condition, current, size);
    }

    @Override
    public long selectCountByCondition(T condition) {
        // 实现根据条件的计数查询
        if (condition == null) {
            // 如果条件为空，返回总记录数
            return redisOrmTemplate.count(entityClass);
        }
        // 使用条件进行计数查询
        return redisOrmTemplate.countByCondition(entityClass, condition);
    }
}