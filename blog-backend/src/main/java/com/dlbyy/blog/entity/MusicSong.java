package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 歌曲实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("music_song")
public class MusicSong extends BaseEntity {

    /** 歌曲名 */
    private String title;

    /** 歌手 */
    private String artist;

    /** 专辑 */
    private String album;

    /** 时长（秒） */
    private Integer duration;

    /** 封面URL */
    private String cover;

    /** 音频文件URL */
    private String fileUrl;

    /** 格式 */
    private String format;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 歌词（LRC 格式） */
    private String lyric;

    /** 播放次数 */
    private Integer playCount;
}
