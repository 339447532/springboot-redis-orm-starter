package com.asd.redis.orm.mapper;

import com.asd.redis.orm.model.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 基础Mapper接口
 */
public interface BaseMapper<T> {
    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return 实体对象
     */
    T insert(T entity);

    /**
     * 批量插入记录
     *
     * @param entityList 实体对象集合
     * @return 实体对象集合
     */
    List<T> insertBatch(Collection<T> entityList);

    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     * @return 是否成功
     */
    boolean deleteById(Serializable id);

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键ID列表
     * @return 是否成功
     */
    boolean deleteBatchIds(Collection<? extends Serializable> idList);

    /**
     * 根据 ID 修改
     *
     * @param entity 实体对象
     * @return 是否成功
     */
    boolean updateById(T entity);

    /**
     * 根据ID 批量更新
     *
     * @param entityList 实体对象集合
     * @return 是否成功
     */
    boolean updateBatchById(Collection<T> entityList);

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     * @return 实体
     */
    T selectById(Serializable id);

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键ID列表
     * @return 实体集合
     */
    List<T> selectBatchIds(Collection<? extends Serializable> idList);

    /**
     * 分页查询
     *
     * @param current 当前页
     * @param size    每页显示条数
     * @return 分页对象
     */
    Page<T> selectPage(long current, long size);

    /**
     * 查询总记录数
     *
     * @return 总数
     */
    long selectCount();

    /**
     * 根据条件对象查询列表
     *
     * @param condition 条件对象，非空字段将作为查询条件
     * @return 实体集合
     */
    List<T> selectByCondition(T condition);

    /**
     * 根据条件对象查询分页
     *
     * @param condition 条件对象，非空字段将作为查询条件
     * @param current   当前页
     * @param size      每页显示条数
     * @return 分页对象
     */
    Page<T> selectPageByCondition(T condition, long current, long size);

    /**
     * 根据条件对象查询总记录数
     *
     * @param condition 条件对象，非空字段将作为查询条件
     * @return 总数
     */
    long selectCountByCondition(T condition);

    /**
     * 根据条件查询并排序
     *
     * @param condition 查询条件
     * @param orderBy   排序字段
     * @param isAsc     是否升序
     * @return 排序后的结果列表
     */
    List<T> selectByCondition(T condition, String orderBy, boolean isAsc);

    /**
     * 根据条件分页查询并排序
     *
     * @param condition 查询条件
     * @param current   当前页
     * @param size      每页大小
     * @param orderBy   排序字段
     * @param isAsc     是否升序
     * @return 分页结果
     */
    Page<T> selectPageByCondition(T condition, long current, long size, String orderBy, boolean isAsc);
}