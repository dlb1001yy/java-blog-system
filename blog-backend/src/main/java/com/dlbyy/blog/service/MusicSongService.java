package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.MusicSong;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 歌曲服务
 */
public interface MusicSongService extends IService<MusicSong> {

    /**
     * 上传音频文件并保存歌曲（仅支持 mp3，大小 ≤ 20MB；duration 不解析，允许为空）
     *
     * @param lyric 歌词（可为空）
     * @param cover 封面 URL（可为空；为空时自动生成渐变封面）
     */
    MusicSong uploadAndSave(MultipartFile file, String title, String artist, String album, String lyric, String cover);

    /**
     * 管理端保存（id 为空新增，否则更新；上传后传入 fileUrl/size/format 元数据）
     *
     * @return 歌曲ID
     */
    Long adminSave(MusicSong song);

    /**
     * 管理端删除
     */
    void adminDelete(Long id);

    /**
     * 播放（播放次数 +1）
     */
    void play(Long id);

    /**
     * 存储统计：totalSongs 总曲目 / totalDuration 总时长(秒) / totalSize 总大小(字节) / totalPlayCount 总播放
     */
    Map<String, Object> storageStats();
}
