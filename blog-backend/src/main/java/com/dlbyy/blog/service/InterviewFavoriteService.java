package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.InterviewFavorite;

/**
 * 面试题收藏/错题本服务
 */
public interface InterviewFavoriteService extends IService<InterviewFavorite> {

    /**
     * 收藏 / 取消收藏（type 0 收藏 1 错题）
     *
     * @return true 收藏成功；false 取消收藏
     */
    boolean toggle(Long userId, Long questionId, Integer type);

    /**
     * 按用户与类型分页查询题目（填充 question 对象）
     */
    PageResult<InterviewFavorite> pageByUser(int page, int size, Long userId, Integer type);
}
