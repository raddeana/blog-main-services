package com.blog.services.contents.controllers;

import com.blog.services.common.Result;
import com.blog.services.contents.models.dto.CommentDTO;
import com.blog.services.contents.models.vo.CreateCommentVO;
import com.blog.services.contents.services.CommentService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容评论控制器
 *
 * RESTful API设计：
 * GET    /api/contents/{contentId}/comments           查询某内容的评论列表（树形）
 * POST   /api/contents/{contentId}/comments           发表评论/回复
 * DELETE /api/contents/{contentId}/comments/{id}      删除评论（含子回复）
 */
@RestController
@RequestMapping("/api/contents/{contentId}/comments")
public class CommentsController {

    @Resource
    private CommentService commentService;

    /**
     * 查询某内容的评论列表（树形结构）
     */
    @GetMapping
    public Result<List<CommentDTO>> listComments(@PathVariable("contentId") Long contentId) {
        if (contentId == null || contentId <= 0) {
            return Result.badRequest("内容ID无效");
        }
        List<CommentDTO> comments = commentService.listComments(contentId);
        return Result.success(comments);
    }

    /**
     * 发表评论或回复
     */
    @PostMapping
    public Result<CommentDTO> createComment(
            @PathVariable("contentId") Long contentId,
            @RequestBody CreateCommentVO vo) {
        if (contentId == null || contentId <= 0) {
            return Result.badRequest("内容ID无效");
        }
        if (vo == null) {
            return Result.badRequest("请求体不能为空");
        }
        if (vo.getAuthorId() == null || vo.getAuthorId() <= 0) {
            return Result.badRequest("评论者ID无效");
        }
        if (StringUtils.isBlank(vo.getContent())) {
            return Result.badRequest("评论内容不能为空");
        }
        try {
            CommentDTO comment = commentService.createComment(contentId, vo);
            return Result.success("评论发表成功", comment);
        } catch (Exception e) {
            return Result.error("评论发表失败：" + e.getMessage());
        }
    }

    /**
     * 删除评论（含其所有子回复）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @PathVariable("contentId") Long contentId,
            @PathVariable("id") Long id) {
        if (contentId == null || contentId <= 0) {
            return Result.badRequest("内容ID无效");
        }
        if (id == null || id <= 0) {
            return Result.badRequest("评论ID无效");
        }
        try {
            boolean success = commentService.deleteComment(id);
            if (!success) {
                return Result.notFound("评论不存在，ID: " + id);
            }
            return Result.success("评论删除成功", null);
        } catch (Exception e) {
            return Result.error("评论删除失败：" + e.getMessage());
        }
    }
}
