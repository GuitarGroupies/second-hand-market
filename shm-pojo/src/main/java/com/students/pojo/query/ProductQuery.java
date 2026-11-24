package com.students.pojo.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 商品列表查询入参。
 *
 * <p>继承 {@link PageQuery} 获得分页参数，本类只放筛选条件。
 * 所有字段都是可选条件，为 null 时不参与 SQL 拼接。</p>
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 分类 ID，为空表示全部分类 */
    private Long categoryId;

    /** 关键词，匹配标题，为空表示不筛 */
    private String keyword;

    /** 最低价 */
    private BigDecimal minPrice;

    /** 最高价 */
    private BigDecimal maxPrice;

    /** 排序方式：new（最新）/ price_asc（价格升序）/ price_desc（价格降序） */
    private String sortBy;
}