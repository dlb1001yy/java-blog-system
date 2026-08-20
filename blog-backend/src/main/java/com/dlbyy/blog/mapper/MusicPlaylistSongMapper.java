package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.MusicPlaylistSong;
import org.apache.ibatis.annotations.Mapper;

/**
 * 歌单-歌曲关联 Mapper
 */
@Mapper
public interface MusicPlaylistSongMapper extends BaseMapper<MusicPlaylistSong> {
}
