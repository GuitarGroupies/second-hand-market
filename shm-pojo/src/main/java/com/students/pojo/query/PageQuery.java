package com.students.pojo.query;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 分页查询基类。
 *
 * <p>所有列表查询的入参对象继承本类即可获得分页参数，
 * 并通过 {@link #getSafePageNum()} / {@link #getSafePageSize()} 获得经过边界保护的值，
 * 避免前端传入 0、负数或超大值导致越界查询或内存溢出。</p>
 *
 * @author students
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 每页最大条数，防止一次拉全表 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 默认每页条数 */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /** 页码，从 1 开始 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 获取安全的页码。
     *
     * @return 至少为 1 的页码
     */
    public Integer getSafePageNum() {
        return Objects.isNull(pageNum) || pageNum < 1 ? 1 : pageNum;
    }

    /**
     * 获取安全的每页条数，限制在 1~100。
     *
     * @return 合法条数
     */
    public Integer getSafePageSize() {
        if (Objects.isNull(pageSize) || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
