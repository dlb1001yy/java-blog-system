package com.dlbyy.blog.service;

/**
 * 在线歌词服务：按「歌名+歌手」从网易云音乐公开接口搜索并获取 LRC 歌词
 */
public interface OnlineLyricService {

    /**
     * 在线获取 LRC 歌词，获取失败或无匹配返回 null（不抛异常，不影响主流程）
     */
    String fetchLrc(String title, String artist);
}
