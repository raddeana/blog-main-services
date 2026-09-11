package com.blog.services.contents.controllers;

import com.blog.services.common.Result;
import com.blog.services.contents.models.dto.CommentDTO;
import com.blog.services.contents.models.vo.CreateCommentVO;
import com.blog.services.contents.services.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * CommentsController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class CommentsControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentsController controller;

    // ==================== listComments ====================

    @Test
    void listComments_validContentId_returns200() {
        when(commentService.listComments(1L)).thenReturn(Collections.emptyList());

        Result<List<CommentDTO>> result = controller.listComments(1L);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    void listComments_invalidContentId_returns400() {
        Result<List<CommentDTO>> result = controller.listComments(0L);

        assertThat(result.getCode()).isEqualTo(400);
        verify(commentService, never()).listComments(any());
    }

    // ==================== createComment ====================

    @Test
    void createComment_validInput_returns200() {
        CreateCommentVO vo = new CreateCommentVO();
        vo.setAuthorId(1L);
        vo.setContent("好文章");

        CommentDTO dto = new CommentDTO();
        dto.setId(1L);
        when(commentService.createComment(eq(10L), any(CreateCommentVO.class))).thenReturn(dto);

        Result<CommentDTO> result = controller.createComment(10L, vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).contains("成功");
    }

    @Test
    void createComment_nullBody_returns400() {
        Result<CommentDTO> result = controller.createComment(1L, null);

        assertThat(result.getCode()).isEqualTo(400);
        verify(commentService, never()).createComment(any(), any());
    }

    @Test
    void createComment_invalidContentId_returns400() {
        CreateCommentVO vo = new CreateCommentVO();
        vo.setAuthorId(1L);
        vo.setContent("评论");

        Result<CommentDTO> result = controller.createComment(-1L, vo);

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void createComment_invalidAuthorId_returns400() {
        CreateCommentVO vo = new CreateCommentVO();
        vo.setAuthorId(0L);
        vo.setContent("评论");

        Result<CommentDTO> result = controller.createComment(1L, vo);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("评论者ID");
    }

    @Test
    void createComment_blankContent_returns400() {
        CreateCommentVO vo = new CreateCommentVO();
        vo.setAuthorId(1L);
        vo.setContent("");

        Result<CommentDTO> result = controller.createComment(1L, vo);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("评论内容");
    }

    // ==================== deleteComment ====================

    @Test
    void deleteComment_existing_returns200() {
        when(commentService.deleteComment(1L)).thenReturn(true);

        Result<Void> result = controller.deleteComment(10L, 1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).contains("成功");
    }

    @Test
    void deleteComment_notFound_returns404() {
        when(commentService.deleteComment(999L)).thenReturn(false);

        Result<Void> result = controller.deleteComment(10L, 999L);

        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void deleteComment_invalidCommentId_returns400() {
        Result<Void> result = controller.deleteComment(10L, 0L);

        assertThat(result.getCode()).isEqualTo(400);
        verify(commentService, never()).deleteComment(any());
    }
}
