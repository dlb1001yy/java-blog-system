package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.MusicSong;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 歌曲 Mapper
 */
@Mapper
public interface MusicSongMapper extends BaseMapper<MusicSong> {

    /**
     * 原子自增播放次数
     */
    @Update("UPDATE music_song SET play_count = play_count + 1 WHERE id = #{id}")
    int addPlayCount(@Param("id") Long id);
}
