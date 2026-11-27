package com.students.pojo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布入参。
 *
 * @author students
 */
@Data
public class ProductCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 分类 ID */
    @NotNull(message = "请选择分类")
    private Long categoryId;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多 100 个字符")
    private String title;

    /** 描述 */
    @Size(max = 2000, message = "描述最多 2000 个字符")
    private String description;

    /** 售价 */
    @NotNull(message = "请填写售价")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    /** 原价 */
    @DecimalMin(value = "0", message = "原价不能为负数")
    private BigDecimal originalPrice;

    /** 库存，默认 1 */
    private Integer stock;

    /** 图片地址列表，第一张作为封面 */
    @Size(max = 9, message = "最多上传 9 张图片")
    private List<String> images;

    /** 标签，最多 10 个，每个最多 20 字 */
    @Size(max = 10, message = "最多添加 10 个标签")
    private List<String> tags;
}
