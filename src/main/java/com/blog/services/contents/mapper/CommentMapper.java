package com.blog.services.contents.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.services.contents.models.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论 Mapper 接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
