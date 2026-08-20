package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.MusicPlaylist;

import java.util.List;

/**
 * 歌单服务
 */
public interface MusicPlaylistService extends IService<MusicPlaylist> {

    /**
     * 管理端分页
     */
    PageResult<MusicPlaylist> adminPage(int page, int size, String keyword);

    /**
     * 保存歌单基本信息（id 为空新增）
     *
     * @return 歌单ID
     */
    Long adminSave(MusicPlaylist playlist);

    /**
     * 删除歌单（同时删除歌单-歌曲关联）
     */
    void adminDelete(Long playlistId);

    /**
     * 维护歌单歌曲关联（全量替换，按列表顺序设置 sort_order）
     */
    void saveSongs(Long playlistId, List<Long> songIds);

    /**
     * 门户歌单列表（已发布，含歌曲数）
     */
    PageResult<MusicPlaylist> portalPage(int page, int size);

    /**
     * 门户歌单详情（含歌曲列表）
     */
    MusicPlaylist portalDetail(Long playlistId);
}
