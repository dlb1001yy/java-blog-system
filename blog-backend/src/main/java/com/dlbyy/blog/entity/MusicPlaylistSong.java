package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 歌单-歌曲关联实体
 * <p>
 * 该表无逻辑删除与更新时间字段，不继承 {@link BaseEntity}。
 */
@Data
@TableName("music_playlist_song")
public class MusicPlaylistSong implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 歌单ID */
    private Long playlistId;

    /** 歌曲ID */
    private Long songId;

    /** 顺序 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
