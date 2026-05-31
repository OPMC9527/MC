package com.edulink.utils;

import lombok.Data;
import java.util.List;

/**
 * 分页结果封装类
 * 用于统一封装分页查询的返回结果，包含总记录数和当前页数据列表
 *
 * @param <T> 数据列表中元素的类型
 */
@Data
public class PageResult<T> {

    /**
     * 总记录数（符合查询条件的全部记录条数）
     */
    private Long total;

    /**
     * 当前页的数据列表（根据分页参数截取）
     */
    private List<T> rows;

    /**
     * 构造分页结果对象
     *
     * @param total 总记录数
     * @param rows  当前页数据列表
     */
    public PageResult(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }
}