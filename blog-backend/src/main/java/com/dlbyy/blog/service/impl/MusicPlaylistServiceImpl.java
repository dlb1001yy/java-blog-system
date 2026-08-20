package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.MusicPlaylist;
import com.dlbyy.blog.entity.MusicPlaylistSong;
import com.dlbyy.blog.entity.MusicSong;
import com.dlbyy.blog.mapper.MusicPlaylistMapper;
import com.dlbyy.blog.mapper.MusicPlaylistSongMapper;
import com.dlbyy.blog.mapper.MusicSongMapper;
import com.dlbyy.blog.service.MusicPlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 歌单服务实现
 */
@Service
@RequiredArgsConstructor
public class MusicPlaylistServiceImpl extends ServiceImpl<MusicPlaylistMapper, MusicPlaylist>
        implements MusicPlaylistService {

    private final MusicPlaylistSongMapper musicPlaylistSongMapper;
    private final MusicSongMapper musicSongMapper;

    @Override
    public PageResult<MusicPlaylist> adminPage(int page, int size, String keyword) {
        Page<MusicPlaylist> p = new Page<>(page, size);
        this.page(p, new LambdaQueryWrapper<MusicPlaylist>()
                .like(StringUtils.hasText(keyword), MusicPlaylist::getName, keyword)
                .orderByDesc(MusicPlaylist::getUpdateTime));
        fillSongCount(p.getRecords());
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public Long adminSave(MusicPlaylist playlist) {
        if (playlist.getId() == null) {
            this.save(playlist);
        } else {
            this.updateById(playlist);
        }
        return playlist.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Long playlistId) {
        this.removeById(playlistId);
        musicPlaylistSongMapper.delete(new LambdaQueryWrapper<MusicPlaylistSong>()
                .eq(MusicPlaylistSong::getPlaylistId, playlistId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSongs(Long playlistId, List<Long> songIds) {
        musicPlaylistSongMapper.delete(new LambdaQueryWrapper<MusicPlaylistSong>()
                .eq(MusicPlaylistSong::getPlaylistId, playlistId));
        if (songIds == null || songIds.isEmpty()) {
            return;
        }
        Set<Long> distinct = new HashSet<>();
        int sort = 0;
        for (Long songId : songIds) {
            if (!distinct.add(songId)) {
                continue; // 去重，防唯一键冲突
            }
            MusicPlaylistSong rel = new MusicPlaylistSong();
            rel.setPlaylistId(playlistId);
            rel.setSongId(songId);
            rel.setSortOrder(sort++);
            musicPlaylistSongMapper.insert(rel);
        }
    }

    @Override
    public PageResult<MusicPlaylist> portalPage(int page, int size) {
        IPage<MusicPlaylist> p = baseMapper.selectListWithSongCount(new Page<>(page, size), 1);
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public MusicPlaylist portalDetail(Long playlistId) {
        MusicPlaylist playlist = this.getById(playlistId);
        if (playlist == null || playlist.getStatus() == null || playlist.getStatus() != 1) {
            return null;
        }
        List<MusicPlaylistSong> rels = musicPlaylistSongMapper.selectList(
                new LambdaQueryWrapper<MusicPlaylistSong>()
                        .eq(MusicPlaylistSong::getPlaylistId, playlistId)
                        .orderByAsc(MusicPlaylistSong::getSortOrder));
        if (rels.isEmpty()) {
            playlist.setSongCount(0);
            playlist.setSongList(List.of());
            return playlist;
        }
        Map<Long, MusicSong> songMap = musicSongMapper.selectBatchIds(
                        rels.stream().map(MusicPlaylistSong::getSongId).collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(MusicSong::getId, Function.identity()));
        List<MusicSong> songs = rels.stream()
                .map(rel -> songMap.get(rel.getSongId()))
                .filter(song -> song != null)
                .collect(Collectors.toList());
        playlist.setSongCount(songs.size());
        playlist.setSongList(songs);
        return playlist;
    }

    /** 填充歌曲数量 */
    private void fillSongCount(List<MusicPlaylist> playlists) {
        for (MusicPlaylist playlist : playlists) {
            Long count = musicPlaylistSongMapper.selectCount(new LambdaQueryWrapper<MusicPlaylistSong>()
                    .eq(MusicPlaylistSong::getPlaylistId, playlist.getId()));
            playlist.setSongCount(count == null ? 0 : count.intValue());
        }
    }
}
