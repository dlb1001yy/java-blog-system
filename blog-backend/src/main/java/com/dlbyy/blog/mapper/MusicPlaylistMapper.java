package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.entity.MusicPlaylist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 歌单 Mapper
 */
@Mapper
public interface MusicPlaylistMapper extends BaseMapper<MusicPlaylist> {

    /**
     * 歌单列表（含歌曲数），自定义 SQL 见 MusicPlaylistMapper.xml
     */
    IPage<MusicPlaylist> selectListWithSongCount(Page<MusicPlaylist> page, @Param("status") Integer status);
}
