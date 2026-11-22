package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品分类实体，对应表 {@code product_category}。
 *
 * <p>两级结构：{@code parentId = 0} 表示一级分类，其余表示挂在某个一级分类下。</p>
 *
 * @author students
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_category")
public class ProductCategory extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 父分类 ID，0 表示一级分类 */
    private Long parentId;

    /** 分类名称 */
    private String name;

    /** 图标地址 */
    private String icon;

    /** 排序值，越小越靠前 */
    private Integer sort;
}
