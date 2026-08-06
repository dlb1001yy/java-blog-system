package com.dlbyy.blog.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>
 * 配合实体字段上的 @TableField(fill = FieldFill.INSERT) /
 * @TableField(fill = FieldFill.INSERT_UPDATE) 使用：
 * <ul>
 *     <li>INSERT 时自动填充 createTime 与 updateTime</li>
 *     <li>UPDATE 时自动填充 updateTime</li>
 * </ul>
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    private static final String FIELD_CREATE_TIME = "createTime";
    private static final String FIELD_UPDATE_TIME = "updateTime";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, FIELD_CREATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
