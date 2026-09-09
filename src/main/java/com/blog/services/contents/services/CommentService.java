package com.blog.services.contents.services;

import com.blog.services.contents.models.dto.CommentDTO;
import com.blog.services.contents.models.vo.CreateCommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 发表评论
     *
     * @param contentId 内容ID
     * @param vo        评论请求
     * @return 评论DTO
     */
    CommentDTO createComment(Long contentId, CreateCommentVO vo);

    /**
     * 查询某内容的评论列表（树形结构，顶级评论含子回复）
     *
     * @param contentId 内容ID
     * @return 评论树
     */
    List<CommentDTO> listComments(Long contentId);

    /**
     * 删除评论（同时删除其所有子回复）
     *
     * @param commentId 评论ID
     * @return 是否删除成功
     */
    boolean deleteComment(Long commentId);
}
