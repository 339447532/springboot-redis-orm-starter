package com.asd.redis.orm.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页模型
 */
@Data
public class Page<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private long current;

    /**
     * 每页大小
     */
    private long size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 记录列表
     */
    private List<T> records = new ArrayList<>();

    public Page() {
        this.current = 1;
        this.size = 10;
    }

    public Page(long current, long size) {
        this.current = current > 0 ? current : 1;
        this.size = size > 0 ? size : 10;
    }

    public boolean hasNext() {
        return this.current < this.pages;
    }

    public boolean hasPrevious() {
        return this.current > 1;
    }
}