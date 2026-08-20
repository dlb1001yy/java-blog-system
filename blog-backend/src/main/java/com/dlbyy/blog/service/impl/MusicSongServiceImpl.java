package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.MusicSong;
import com.dlbyy.blog.mapper.MusicSongMapper;
import com.dlbyy.blog.service.MusicSongService;
import com.dlbyy.blog.storage.FileStorageService;
import com.dlbyy.blog.storage.FileUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 歌曲服务实现
 */
@Service
@RequiredArgsConstructor
public class MusicSongServiceImpl extends ServiceImpl<MusicSongMapper, MusicSong> implements MusicSongService {

    private final FileStorageService fileStorageService;

    /** mp3 大小上限：20MB */
    private static final long MAX_AUDIO_SIZE = 20L * 1024 * 1024;

    @Override
    public MusicSong uploadAndSave(MultipartFile file, String title, String artist, String album) {
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
        this.removeById(id);
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
