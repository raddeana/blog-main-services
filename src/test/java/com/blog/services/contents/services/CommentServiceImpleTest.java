package com.blog.services.contents.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.services.contents.mapper.CommentMapper;
import com.blog.services.contents.models.Comment;
import com.blog.services.contents.models.dto.CommentDTO;
import com.blog.services.contents.models.vo.CreateCommentVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CommentServiceImple 单元测试
 * <p>
 * 重点验证树形组装逻辑和级联删除子回复。
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceImpleTest {

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImple commentService;

    // ==================== createComment ====================

    @Test
    void createComment_topLevel_parentIdDefaultsToZero() {
        CreateCommentVO vo = new CreateCommentVO();
        vo.setAuthorId(1L);
        vo.setContent("顶级评论");

        doAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(1L);
            return 1;
        }).when(commentMapper).insert(any(Comment.class));

        CommentDTO result = commentService.createComment(10L, vo);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContentId()).isEqualTo(10L);
        assertThat(result.getParentId()).isEqualTo(0L);
        assertThat(result.getContent()).isEqualTo("顶级评论");
    }

    @Test
    void createComment_reply_preservesParentId() {
        CreateCommentVO vo = new CreateCommentVO();
        vo.setAuthorId(2L);
        vo.setContent("回复");
        vo.setParentId(5L);

        doAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(2L);
            return 1;
        }).when(commentMapper).insert(any(Comment.class));

        CommentDTO result = commentService.createComment(10L, vo);

        assertThat(result.getParentId()).isEqualTo(5L);
    }

    @Test
    void createComment_negativeParentId_defaultsToZero() {
        CreateCommentVO vo = new CreateCommentVO();
        vo.setAuthorId(1L);
        vo.setContent("测试");
        vo.setParentId(-1L);

        doAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(1L);
            return 1;
        }).when(commentMapper).insert(any(Comment.class));

        CommentDTO result = commentService.createComment(10L, vo);

        assertThat(result.getParentId()).isEqualTo(0L);
    }

    // ==================== listComments ====================

    @Test
    @SuppressWarnings("unchecked")
    void listComments_buildsTreeStructure() {
        // 构造：1 条顶级评论 + 2 条回复
        Comment root = buildComment(1L, 10L, 1L, "顶级评论", 0L, 1000L);
        Comment reply1 = buildComment(2L, 10L, 2L, "回复1", 1L, 2000L);
        Comment reply2 = buildComment(3L, 10L, 3L, "回复2", 1L, 3000L);

        when(commentMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(root, reply1, reply2));

        List<CommentDTO> result = commentService.listComments(10L);

        // 1 条顶级评论
        assertThat(result).hasSize(1);
        CommentDTO rootDTO = result.get(0);
        assertThat(rootDTO.getId()).isEqualTo(1L);
        assertThat(rootDTO.getContent()).isEqualTo("顶级评论");

        // 2 条子回复
        assertThat(rootDTO.getChildren()).hasSize(2);
        assertThat(rootDTO.getChildren().get(0).getContent()).isEqualTo("回复1");
        assertThat(rootDTO.getChildren().get(1).getContent()).isEqualTo("回复2");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listComments_multipleRoots_returnsAllRoots() {
        Comment r1 = buildComment(1L, 10L, 1L, "评论1", 0L, 1000L);
        Comment r2 = buildComment(2L, 10L, 2L, "评论2", 0L, 2000L);

        when(commentMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(r1, r2));

        List<CommentDTO> result = commentService.listComments(10L);

        assertThat(result).hasSize(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listComments_orphanReply_parentNotFound_treatedAsRoot() {
        // 回复的 parentId 指向不存在的评论
        Comment orphan = buildComment(1L, 10L, 1L, "孤儿回复", 999L, 1000L);

        when(commentMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Collections.singletonList(orphan));

        List<CommentDTO> result = commentService.listComments(10L);

        // 父评论不存在时，作为顶级展示
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChildren()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void listComments_empty_returnsEmptyList() {
        when(commentMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<CommentDTO> result = commentService.listComments(10L);

        assertThat(result).isEmpty();
    }

    // ==================== deleteComment ====================

    @Test
    void deleteComment_existing_deletesRepliesAndSelf() {
        Comment comment = buildComment(1L, 10L, 1L, "评论", 0L, 1000L);
        when(commentMapper.selectById(1L)).thenReturn(comment);

        boolean result = commentService.deleteComment(1L);

        assertThat(result).isTrue();
        // 删子回复
        verify(commentMapper).delete(any(QueryWrapper.class));
        // 删自身
        verify(commentMapper).deleteById(1L);
    }

    @Test
    void deleteComment_notFound_returnsFalse() {
        when(commentMapper.selectById(999L)).thenReturn(null);

        boolean result = commentService.deleteComment(999L);

        assertThat(result).isFalse();
        verify(commentMapper, never()).delete(any(QueryWrapper.class));
        verify(commentMapper, never()).deleteById(any());
    }

    // ==================== helper ====================

    private Comment buildComment(Long id, Long contentId, Long authorId, String content, Long parentId, Long createTime) {
        Comment c = new Comment();
        c.setId(id);
        c.setContentId(contentId);
        c.setAuthorId(authorId);
        c.setContent(content);
        c.setParentId(parentId);
        c.setCreateTime(createTime);
        c.setUpdateTime(createTime);
        return c;
    }
}
