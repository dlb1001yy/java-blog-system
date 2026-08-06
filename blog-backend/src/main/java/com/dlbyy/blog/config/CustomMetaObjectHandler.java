package com.dlbyy.blog.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>
 * 配合 {@link com.dlbyy.blog.entity.BaseEntity} 使用，统一填充：
 * <ul>
 *     <li>INSERT 时填充 createTime、updateTime、isDeleted(0)</li>
 *     <li>UPDATE 时填充 updateTime</li>
 * </ul>
 * 实体字段需标注 @TableField(fill = FieldFill.INSERT) 或
 * @TableField(fill = FieldFill.INSERT_UPDATE)。
 */
@Slf4j
@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {

    private static final String FIELD_CREATE_TIME = "createTime";
    private static final String FIELD_UPDATE_TIME = "updateTime";
    private static final String FIELD_IS_DELETED = "isDeleted";
    private static final Integer NOT_DELETED = 0;

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, FIELD_CREATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, FIELD_IS_DELETED, Integer.class, NOT_DELETED);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
