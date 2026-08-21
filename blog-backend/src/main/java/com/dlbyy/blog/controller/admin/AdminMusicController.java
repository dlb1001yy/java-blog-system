package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.MusicPlaylist;
import com.dlbyy.blog.entity.MusicSong;
import com.dlbyy.blog.service.MusicPlaylistService;
import com.dlbyy.blog.service.MusicSongService;
import com.dlbyy.blog.service.OnlineLyricService;
import com.dlbyy.blog.utils.Mp3LyricParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 后台音乐管理（歌曲上传/CRUD/统计 + 歌单 CRUD）
 */
@RestController
@RequestMapping("/admin/music")
@RequiredArgsConstructor
@Tag(name = "后台音乐管理")
public class AdminMusicController {

    private final MusicSongService musicSongService;
    private final MusicPlaylistService musicPlaylistService;
    private final OnlineLyricService onlineLyricService;

    // ---------------- 歌曲 ----------------

    @PostMapping("/songs/upload")
    @Admin("上传歌曲")
    @Operation(summary = "上传音频文件并保存歌曲（file + title/artist/album/lyric/cover）")
    public Result<MusicSong> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam String title,
                                    @RequestParam(required = false) String artist,
                                    @RequestParam(required = false) String album,
                                    @RequestParam(required = false) String lyric,
                                    @RequestParam(required = false) String cover) {
        // 统一走 Service 层上传（内置 mp3 格式与 20MB 大小校验），不走通用 FileUtils（其限制为图片/5MB）
        MusicSong song = musicSongService.uploadAndSave(file, title, artist, album, lyric, cover);
        return Result.success("上传成功", song);
    }

    @PostMapping("/songs/parse-lyric")
    @Admin("解析歌词")
    @Operation(summary = "解析歌词：优先 MP3 内嵌（ID3v2 USLT），无内嵌时按歌名+歌手在线匹配 LRC")
    public Result<String> parseLyric(@RequestParam("file") MultipartFile file,
                                     @RequestParam(required = false) String title,
                                     @RequestParam(required = false) String artist) {
        String embedded = Mp3LyricParser.parse(file);
        if (embedded != null && !embedded.isBlank()) {
            return Result.success("内嵌歌词", embedded);
        }
        String online = onlineLyricService.fetchLrc(title, artist);
        if (online != null) {
            return Result.success("在线匹配", online);
        }
        return Result.success("未找到歌词", null);
    }

    @GetMapping("/songs")
    @Operation(summary = "歌曲分页（keyword 匹配歌名/歌手/专辑）")
    public Result<Page<MusicSong>> songs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<MusicSong> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(MusicSong::getTitle, keyword)
                    .or().like(MusicSong::getArtist, keyword)
                    .or().like(MusicSong::getAlbum, keyword));
        }
        wrapper.orderByDesc(MusicSong::getCreateTime);
        return Result.success(musicSongService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/songs/stats")
    @Operation(summary = "歌曲存储统计")
    public Result<Map<String, Object>> songStats() {
        return Result.success(musicSongService.storageStats());
    }

    @PostMapping("/songs")
    @Admin("新增歌曲")
    @Operation(summary = "新增歌曲（外链元数据方式）")
    public Result<Long> createSong(@RequestBody MusicSong song) {
        return Result.success("保存成功", musicSongService.adminSave(song));
    }

    @PutMapping("/songs/{id}")
    @Admin("更新歌曲")
    @Operation(summary = "更新歌曲信息")
    public Result<?> updateSong(@PathVariable Long id, @RequestBody MusicSong song) {
        song.setId(id);
        musicSongService.adminSave(song);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/songs/{id}")
    @Admin("删除歌曲")
    @Operation(summary = "删除歌曲")
    public Result<?> deleteSong(@PathVariable Long id) {
        musicSongService.adminDelete(id);
        return Result.success("删除成功", null);
    }

    // ---------------- 歌单 ----------------

    @GetMapping("/playlists")
    @Operation(summary = "歌单分页")
    public Result<?> playlists(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(musicPlaylistService.adminPage(page, size, keyword));
    }

    @GetMapping("/playlists/{id}")
    @Operation(summary = "歌单详情")
    public Result<MusicPlaylist> playlistDetail(@PathVariable Long id) {
        return Result.success(musicPlaylistService.getById(id));
    }

    @PostMapping("/playlists")
    @Admin("新增歌单")
    @Operation(summary = "新增歌单")
    public Result<Long> createPlaylist(@RequestBody MusicPlaylist playlist) {
        return Result.success("保存成功", musicPlaylistService.adminSave(playlist));
    }

    @PutMapping("/playlists/{id}")
    @Admin("更新歌单")
    @Operation(summary = "更新歌单信息")
    public Result<?> updatePlaylist(@PathVariable Long id, @RequestBody MusicPlaylist playlist) {
        playlist.setId(id);
        musicPlaylistService.adminSave(playlist);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/playlists/{id}")
    @Admin("删除歌单")
    @Operation(summary = "删除歌单（含歌曲关联）")
    public Result<?> deletePlaylist(@PathVariable Long id) {
        musicPlaylistService.adminDelete(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/playlists/{id}/songs")
    @Admin("设置歌单歌曲")
    @Operation(summary = "设置歌单歌曲（body: songIds 全量替换，按顺序排序）")
    public Result<?> savePlaylistSongs(@PathVariable Long id, @RequestBody SongsRequest request) {
        musicPlaylistService.saveSongs(id, request.getSongIds());
        return Result.success("保存成功", null);
    }

    @lombok.Data
    public static class SongsRequest {
        private List<Long> songIds;
    }
}
