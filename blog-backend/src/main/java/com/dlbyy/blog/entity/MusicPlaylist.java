package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 歌单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("music_playlist")
public class MusicPlaylist extends BaseEntity {

    /** 歌单名 */
    private String name;

    /** 封面URL */
    private String cover;

    /** 描述 */
    private String description;

    /** 状态 0:草稿 1:已发布 */
    private Integer status;

    // ---- 非数据库字段 ----

    /** 歌曲数量（查询填充） */
    @TableField(exist = false)
    private Integer songCount;

    /** 歌单歌曲列表（详情填充） */
    @TableField(exist = false)
    private java.util.List<MusicSong> songList;
}
