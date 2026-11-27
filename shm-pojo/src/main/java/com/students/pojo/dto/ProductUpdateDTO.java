package com.students.pojo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 修改商品入参。
 *
 * <p><b>所有字段都是可选的</b>：传 null 表示不修改该字段。
 * 这样前端可以只提交用户实际改动的部分（例如只改价格），
 * 避免「全量覆盖」时把未传的字段误清空。</p>
 *
 * <p>注意：title 用 @Size 而不是 @NotBlank —— 传 null 合法（不修改），
 * 但如果传了就必须是合法长度。</p>
 *
 * @author students
 */
@Data
public class ProductUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 标题，为 null 表示不修改 */
    @Size(min = 1, max = 100, message = "标题长度需在 1-100 之间")
    private String title;

    /** 描述，为 null 表示不修改 */
    @Size(max = 2000, message = "描述最多 2000 个字符")
    private String description;

    /** 售价，为 null 表示不修改 */
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    /** 原价，为 null 表示不修改 */
    @DecimalMin(value = "0", message = "原价不能为负数")
    private BigDecimal originalPrice;

    /** 库存，为 null 表示不修改 */
    private Integer stock;

    /** 分类 ID，为 null 表示不修改 */
    private Long categoryId;

    /** 图片列表，为 null 表示不修改；传空数组表示清空图片 */
    @Size(max = 9, message = "最多上传 9 张图片")
    private List<String> images;

    /** 标签，为 null 表示不修改；传空数组表示清空标签 */
    @Size(max = 10, message = "最多添加 10 个标签")
    private List<String> tags;
}