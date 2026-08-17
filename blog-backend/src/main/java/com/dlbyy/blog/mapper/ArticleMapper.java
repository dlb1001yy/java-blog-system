package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 原子自增浏览量，避免并发读-改-写丢失更新
     */
    @Update("UPDATE blog_article SET view_count = view_count + #{delta} WHERE id = #{id}")
    int addViewCount(@Param("id") Long id, @Param("delta") long delta);

    /**
     * 原子自增点赞数
     */
    @Update("UPDATE blog_article SET like_count = like_count + #{delta} WHERE id = #{id}")
    int addLikeCount(@Param("id") Long id, @Param("delta") long delta);
}
