package com.asd.redis.orm.core;

import com.alibaba.fastjson.JSON;
import com.asd.redis.orm.annotation.RedisEntity;
import com.asd.redis.orm.annotation.RedisId;
import com.asd.redis.orm.config.RedisOrmProperties;
import com.asd.redis.orm.model.Page;
import com.asd.redis.orm.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis ORM 核心模板类
 */
@Slf4j
public class RedisOrmTemplate {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisOrmProperties properties;

    public RedisOrmTemplate(RedisTemplate<String, Object> redisTemplate, RedisOrmProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /**
     * 获取实体的键
     */
    public <T> String getKey(Class<T> entityClass, Object id) {
        RedisEntity annotation = entityClass.getAnnotation(RedisEntity.class);
        String prefix = annotation != null && !StringUtils.isEmpty(annotation.prefix())
                ? annotation.prefix() : entityClass.getSimpleName().toLowerCase();
        return properties.getKeyPrefix() + prefix + ":" + id;
    }

    /**
     * 获取实体的过期时间
     */
    public <T> long getExpire(Class<T> entityClass) {
        RedisEntity annotation = entityClass.getAnnotation(RedisEntity.class);
        return annotation != null && annotation.expire() > 0
                ? annotation.expire() : properties.getDefaultExpireTime();
    }

    /**
     * 生成ID
     */
    private <T> Object generateId(T entity, Field idField) throws IllegalAccessException {
        RedisId redisId = idField.getAnnotation(RedisId.class);
        RedisId.IdType idType = redisId.type();

        Object id = ReflectionUtils.getFieldValue(entity, idField);
        if (id != null && !StringUtils.isEmpty(id.toString())) {
            return id;
        }

        switch (idType) {
            case UUID:
                id = java.util.UUID.randomUUID().toString().replace("-", "");
                break;
            case AUTO:
                String key = properties.getKeyPrefix() + "id:" + entity.getClass().getSimpleName().toLowerCase();
                id = redisTemplate.opsForValue().increment(key);
                break;
            case INPUT:
                throw new IllegalArgumentException("ID must be provided for INPUT type");
            default:
                throw new IllegalArgumentException("Unsupported ID type: " + idType);
        }

        ReflectionUtils.setFieldValue(entity, idField, id);
        return id;
    }

    /**
     * 保存实体
     */
    public <T> T save(T entity) {
        Class<?> entityClass = entity.getClass();
        Field idField = ReflectionUtils.findFieldWithAnnotation(entityClass, RedisId.class);
        if (idField == null) {
            throw new IllegalArgumentException("No @RedisId field found in " + entityClass.getName());
        }

        try {
            Object id = generateId(entity, idField);
            String key = getKey(entityClass, id);
            redisTemplate.opsForValue().set(key, entity);

            long expire = getExpire(entityClass);
            if (expire > 0) {
                redisTemplate.expire(key, expire, TimeUnit.SECONDS);
            }

            return entity;
        } catch (Exception e) {
            log.error("Failed to save entity: {}", entity, e);
            throw new RuntimeException("Failed to save entity", e);
        }
    }

    /**
     * 批量保存实体
     */
    public <T> List<T> saveBatch(Collection<T> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }

        List<T> result = new ArrayList<>();
        for (T entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    /**
     * 根据ID获取实体
     */
    public <T> T getById(Class<T> entityClass, Object id) {
        String key = getKey(entityClass, id);
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj == null) {
            return null;
        }

        if (obj instanceof String) {
            return JSON.parseObject((String) obj, entityClass);
        } else if (entityClass.isInstance(obj)) {
            return entityClass.cast(obj);
        } else {
            String json = JSON.toJSONString(obj);
            return JSON.parseObject(json, entityClass);
        }
    }

    /**
     * 批量获取实体
     */
    public <T> List<T> listByIds(Class<T> entityClass, Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        List<String> keys = ids.stream()
                .map(id -> getKey(entityClass, id))
                .collect(Collectors.toList());

        List<Object> objects = redisTemplate.opsForValue().multiGet(keys);
        if (CollectionUtils.isEmpty(objects)) {
            return new ArrayList<>();
        }

        List<T> result = new ArrayList<>();
        for (Object obj : objects) {
            if (obj != null) {
                if (obj instanceof String) {
                    result.add(JSON.parseObject((String) obj, entityClass));
                } else if (entityClass.isInstance(obj)) {
                    result.add(entityClass.cast(obj));
                } else {
                    String json = JSON.toJSONString(obj);
                    result.add(JSON.parseObject(json, entityClass));
                }
            }
        }

        return result;
    }

    /**
     * 更新实体
     */
    public <T> boolean updateById(T entity) {
        Class<?> entityClass = entity.getClass();
        Field idField = ReflectionUtils.findFieldWithAnnotation(entityClass, RedisId.class);
        if (idField == null) {
            throw new IllegalArgumentException("No @RedisId field found in " + entityClass.getName());
        }

        try {
            Object id = ReflectionUtils.getFieldValue(entity, idField);
            if (id == null || StringUtils.isEmpty(id.toString())) {
                throw new IllegalArgumentException("ID cannot be null or empty for update");
            }

            String key = getKey(entityClass, id);
            if (!redisTemplate.hasKey(key)) {
                return false;
            }

            redisTemplate.opsForValue().set(key, entity);

            long expire = getExpire(entityClass);
            if (expire > 0) {
                redisTemplate.expire(key, expire, TimeUnit.SECONDS);
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to update entity: {}", entity, e);
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    /**
     * 批量更新实体
     */
    public <T> boolean updateBatchById(Collection<T> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return false;
        }

        boolean result = true;
        for (T entity : entities) {
            result = result && updateById(entity);
        }
        return result;
    }

    /**
     * 根据ID删除实体
     */
    public <T> boolean removeById(Class<T> entityClass, Object id) {
        String key = getKey(entityClass, id);
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 批量删除实体
     */
    public <T> boolean removeByIds(Class<T> entityClass, Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }

        List<String> keys = ids.stream()
                .map(id -> getKey(entityClass, id))
                .collect(Collectors.toList());

        Long count = redisTemplate.delete(keys);
        return count != null && count > 0;
    }

    /**
     * 分页查询
     */
    public <T> Page<T> page(Class<T> entityClass, long current, long size) {
        String pattern = properties.getKeyPrefix() +
                (entityClass.getAnnotation(RedisEntity.class) != null &&
                        !StringUtils.isEmpty(entityClass.getAnnotation(RedisEntity.class).prefix()) ?
                        entityClass.getAnnotation(RedisEntity.class).prefix() :
                        entityClass.getSimpleName().toLowerCase()) + ":*";

        Set<String> keys = redisTemplate.keys(pattern);
        if (CollectionUtils.isEmpty(keys)) {
            return new Page<>(current, size);
        }

        long total = keys.size();
        long pages = (total + size - 1) / size;

        if (current > pages) {
            current = pages;
        }

        long start = (current - 1) * size;
        long end = Math.min(start + size, total);

        List<String> pageKeys = new ArrayList<>(keys).subList((int) start, (int) end);
        List<Object> objects = redisTemplate.opsForValue().multiGet(pageKeys);

        List<T> records = new ArrayList<>();
        if (!CollectionUtils.isEmpty(objects)) {
            for (Object obj : objects) {
                if (obj != null) {
                    if (obj instanceof String) {
                        records.add(JSON.parseObject((String) obj, entityClass));
                    } else if (entityClass.isInstance(obj)) {
                        records.add(entityClass.cast(obj));
                    } else {
                        String json = JSON.toJSONString(obj);
                        records.add(JSON.parseObject(json, entityClass));
                    }
                }
            }
        }

        Page<T> page = new Page<>(current, size);
        page.setTotal(total);
        page.setPages(pages);
        page.setRecords(records);

        return page;
    }

    /**
     * 计数
     */
    public <T> long count(Class<T> entityClass) {
        String pattern = properties.getKeyPrefix() +
                (entityClass.getAnnotation(RedisEntity.class) != null &&
                        !StringUtils.isEmpty(entityClass.getAnnotation(RedisEntity.class).prefix()) ?
                        entityClass.getAnnotation(RedisEntity.class).prefix() :
                        entityClass.getSimpleName().toLowerCase()) + ":*";

        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
    }

    /**
     * 根据条件查询实体列表
     */
    public <T> List<T> listByCondition(Class<T> entityClass, T condition) {
        if (condition == null) {
            // 如果条件为空，返回所有实体
            String pattern = getKeyPattern(entityClass);
            Set<String> keys = redisTemplate.keys(pattern);
            if (CollectionUtils.isEmpty(keys)) {
                return new ArrayList<>();
            }
            return getEntitiesByKeys(entityClass, keys);
        }

        // 获取所有实体
        List<T> allEntities = listAll(entityClass);
        if (CollectionUtils.isEmpty(allEntities)) {
            return new ArrayList<>();
        }

        // 通过反射获取条件对象的非空字段
        Map<String, Object> conditions = ReflectionUtils.getNonNullFields(condition);
        if (conditions.isEmpty()) {
            return allEntities;
        }

        // 过滤符合条件的实体
        return allEntities.stream()
                .filter(entity -> matchCondition(entity, conditions))
                .collect(Collectors.toList());
    }

    /**
     * 根据条件分页查询
     */
    public <T> Page<T> pageByCondition(Class<T> entityClass, T condition, long current, long size) {
        List<T> list = listByCondition(entityClass, condition);

        long total = list.size();
        long pages = (total + size - 1) / size;

        if (current > pages && pages > 0) {
            current = pages;
        }

        long start = (current - 1) * size;
        long end = Math.min(start + size, total);

        List<T> records = new ArrayList<>();
        if (start < total) {
            records = list.subList((int) start, (int) end);
        }

        Page<T> page = new Page<>(current, size);
        page.setTotal(total);
        page.setPages(pages);
        page.setRecords(records);

        return page;
    }

    /**
     * 根据条件查询总记录数
     */
    public <T> long countByCondition(Class<T> entityClass, T condition) {
        return listByCondition(entityClass, condition).size();
    }

    /**
     * 获取所有实体
     */
    private <T> List<T> listAll(Class<T> entityClass) {
        String pattern = getKeyPattern(entityClass);
        Set<String> keys = redisTemplate.keys(pattern);
        if (CollectionUtils.isEmpty(keys)) {
            return new ArrayList<>();
        }
        return getEntitiesByKeys(entityClass, keys);
    }

    /**
     * 根据键集合获取实体列表
     */
    private <T> List<T> getEntitiesByKeys(Class<T> entityClass, Set<String> keys) {
        List<Object> objects = redisTemplate.opsForValue().multiGet(new ArrayList<>(keys));
        if (CollectionUtils.isEmpty(objects)) {
            return new ArrayList<>();
        }

        List<T> result = new ArrayList<>();
        for (Object obj : objects) {
            if (obj != null) {
                if (obj instanceof String) {
                    result.add(JSON.parseObject((String) obj, entityClass));
                } else if (entityClass.isInstance(obj)) {
                    result.add(entityClass.cast(obj));
                } else {
                    String json = JSON.toJSONString(obj);
                    result.add(JSON.parseObject(json, entityClass));
                }
            }
        }

        return result;
    }

    /**
     * 获取实体的键模式
     */
    private <T> String getKeyPattern(Class<T> entityClass) {
        RedisEntity annotation = entityClass.getAnnotation(RedisEntity.class);
        String prefix = annotation != null && !StringUtils.isEmpty(annotation.prefix())
                ? annotation.prefix() : entityClass.getSimpleName().toLowerCase();
        return properties.getKeyPrefix() + prefix + ":*";
    }

    /**
     * 判断实体是否匹配条件
     */
    private <T> boolean matchCondition(T entity, Map<String, Object> conditions) {
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String fieldName = entry.getKey();
            Object expectedValue = entry.getValue();

            try {
                Object actualValue = ReflectionUtils.getFieldValue(entity, fieldName);
                if (actualValue == null || !actualValue.equals(expectedValue)) {
                    return false;
                }
            } catch (Exception e) {
                log.error("Failed to get field value: {}", fieldName, e);
                return false;
            }
        }

        return true;
    }
}