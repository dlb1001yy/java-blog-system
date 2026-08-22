package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.MusicPlaylistSong;
import com.dlbyy.blog.entity.MusicSong;
import com.dlbyy.blog.mapper.MusicPlaylistSongMapper;
import com.dlbyy.blog.mapper.MusicSongMapper;
import com.dlbyy.blog.service.MusicSongService;
import com.dlbyy.blog.service.OnlineLyricService;
import com.dlbyy.blog.storage.FileStorageService;
import com.dlbyy.blog.storage.FileUploadResult;
import com.dlbyy.blog.utils.CoverImageGenerator;
import com.dlbyy.blog.utils.Mp3LyricParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 歌曲服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MusicSongServiceImpl extends ServiceImpl<MusicSongMapper, MusicSong> implements MusicSongService {

    private final FileStorageService fileStorageService;
    private final MusicPlaylistSongMapper musicPlaylistSongMapper;
    private final CoverImageGenerator coverImageGenerator;
    private final OnlineLyricService onlineLyricService;

    /** mp3 大小上限：20MB */
    private static final long MAX_AUDIO_SIZE = 20L * 1024 * 1024;

    @Override
    public MusicSong uploadAndSave(MultipartFile file, String title, String artist, String album, String lyric, String cover) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("音频文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        boolean isMp3 = "mp3".equals(suffix)
                || "audio/mpeg".equals(contentType)
                || "audio/mp3".equals(contentType);
        if (!isMp3) {
            throw new BusinessException("仅支持上传 mp3 格式的音频文件");
        }
        if (file.getSize() > MAX_AUDIO_SIZE) {
            throw new BusinessException("音频文件过大，最大允许 20MB");
        }
        if (title == null || title.isBlank()) {
            throw new BusinessException("歌曲名不能为空");
        }

        FileUploadResult uploadResult = fileStorageService.upload(file, "music");
        MusicSong song = new MusicSong();
        song.setTitle(title.trim());
        song.setArtist(artist);
        song.setAlbum(album);
        song.setFileUrl(uploadResult.getUrl());
        song.setFormat("mp3");
        song.setFileSize(file.getSize());
        song.setPlayCount(0);
        // 解析 mp3 时长（按首个 MPEG 帧头比特率估算）
        song.setDuration(Mp3LyricParser.parseDurationSeconds(file));
        // 歌词兜底：未提供时先解析 MP3 内嵌歌词，再按歌名+歌手在线匹配 LRC
        if (lyric == null || lyric.isBlank()) {
            lyric = Mp3LyricParser.parse(file);
            if (lyric == null || lyric.isBlank()) {
                lyric = onlineLyricService.fetchLrc(title.trim(), artist);
            }
        }
        song.setLyric((lyric == null || lyric.isBlank()) ? null : lyric);
        // 未提供封面时自动生成渐变封面（歌名+歌手）
        if (cover == null || cover.isBlank()) {
            try {
                cover = coverImageGenerator.generateSquare(title.trim(), artist, 500);
            } catch (Exception e) {
                log.warn("自动生成歌曲封面失败：{}", e.getMessage());
                cover = null;
            }
        }
        song.setCover(cover);
        this.save(song);
        return song;
    }

    @Override
    public Long adminSave(MusicSong song) {
        if (song.getId() == null) {
            this.save(song);
        } else {
            this.updateById(song);
        }
        return song.getId();
    }

    @Override
    public void adminDelete(Long id) {
        MusicSong song = this.getById(id);
        this.removeById(id);
        // 同步清理歌单关联，避免残留关联导致歌单歌曲数不减
        musicPlaylistSongMapper.delete(new LambdaQueryWrapper<MusicPlaylistSong>()
                .eq(MusicPlaylistSong::getSongId, id));
        // 删除 DB 记录后同步清理存储文件（音频 + 封面）；删除失败仅记日志，不影响业务
        if (song != null) {
            fileStorageService.delete(song.getFileUrl());
            fileStorageService.delete(song.getCover());
        }
    }

    @Override
    public void play(Long id) {
        baseMapper.addPlayCount(id);
    }

    @Override
    public Map<String, Object> storageStats() {
        List<MusicSong> songs = this.list();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSongs", songs.size());
        stats.put("totalDuration", songs.stream()
                .map(s -> s.getDuration() == null ? 0 : s.getDuration()).mapToLong(Integer::longValue).sum());
        stats.put("totalSize", songs.stream()
                .map(s -> s.getFileSize() == null ? 0L : s.getFileSize()).mapToLong(Long::longValue).sum());
        stats.put("totalPlayCount", songs.stream()
                .map(s -> s.getPlayCount() == null ? 0 : s.getPlayCount()).mapToInt(Integer::intValue).sum());
        return stats;
    }
}
