package com.students.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分类树节点，用于分类树接口返回。
 *
 * <p>只暴露前端需要的字段，不直接把实体返回出去——实体里可能有内部字段
 * （如 deleted、createTime），直接返回会泄露无关信息，也不利于接口稳定。</p>
 *
 * @author students
 */
@Data
public class CategoryTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 分类 ID */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 图标地址 */
    private String icon;

    /** 子分类列表，没有子分类时为空集合而不是 null */
    private List<CategoryTreeVO> children;
}
