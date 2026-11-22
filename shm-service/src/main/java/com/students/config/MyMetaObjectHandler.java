package com.students.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公共字段自动填充处理器。
 *
 * <p>实体字段上标注 {@code @TableField(fill = FieldFill.INSERT)} 时，MyBatis-Plus 在执行
 * insert 前会调用本类，把字段值填进去。注意：<b>只有通过 MyBatis-Plus 的标准方法</b>
 * （insert / insertBatch 等）才会触发；自己手写 {@code INSERT ...} 的 XML 不会触发。</p>
 *
 * <p>使用 {@code strictInsertFill} 而不是 {@code setFieldValByName}，前者会判断字段
 * 是否已手动赋值，已有值时不覆盖，避免覆盖调用方显式设置的时间。</p>
 *
 * @author students
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
