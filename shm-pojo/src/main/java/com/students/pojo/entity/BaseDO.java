package com.students.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类，所有表实体统一继承。
 *
 * <p>四个公共字段：</p>
 * <ul>
 *     <li>{@code id}：自增主键，{@code IdType.AUTO} 表示交给数据库自增</li>
 *     <li>{@code createTime} / {@code updateTime}：由 {@code MetaObjectHandler} 自动填充</li>
 *     <li>{@code deleted}：逻辑删除标记，{@code @TableLogic} 会让查询自动追加 {@code deleted = 0}</li>
 * </ul>
 *
 * @author students
 */
@Data
public abstract class BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入与更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记：0 未删除，1 已删除 */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
