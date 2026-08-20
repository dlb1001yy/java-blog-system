package com.dlbyy.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.annotation.RateLimit;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.MusicPlaylist;
import com.dlbyy.blog.entity.MusicSong;
import com.dlbyy.blog.service.MusicPlaylistService;
import com.dlbyy.blog.service.MusicSongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 门户音乐播放器接口（全部公开，播放计数限流防刷）
 */
@RestController
@RequestMapping("/portal/music")
@RequiredArgsConstructor
@Tag(name = "前台音乐接口")
public class PortalMusicController {

    private final MusicPlaylistService musicPlaylistService;
    private final MusicSongService musicSongService;

    @GetMapping("/playlists")
    @Operation(summary = "歌单列表（已发布，含歌曲数）")
    public Result<?> playlists(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(musicPlaylistService.portalPage(page, size));
    }

    @GetMapping("/playlists/{id}")
    @Operation(summary = "歌单详情（含歌曲列表）")
    public Result<MusicPlaylist> playlistDetail(@PathVariable Long id) {
        return Result.success(musicPlaylistService.portalDetail(id));
    }

    @GetMapping("/songs")
    @Operation(summary = "全部歌曲分页（供播放器）")
    public Result<Page<MusicSong>> songs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "100") Integer size) {
        return Result.success(musicSongService.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<MusicSong>().orderByDesc(MusicSong::getCreateTime)));
    }

    @PostMapping("/songs/{id}/play")
    @RateLimit(key = "music-play", time = 60, count = 100)
    @Operation(summary = "播放计数 +1")
    public Result<?> play(@PathVariable Long id) {
        musicSongService.play(id);
        return Result.success();
    }
}
