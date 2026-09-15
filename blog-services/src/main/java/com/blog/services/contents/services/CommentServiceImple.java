package com.blog.services.contents.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.services.contents.mapper.CommentMapper;
import com.blog.services.contents.models.Comment;
import com.blog.services.contents.models.dto.CommentDTO;
import com.blog.services.contents.models.vo.CreateCommentVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论服务实现类
 *
 * 评论支持两级结构：顶级评论（parentId=0）和回复（parentId=顶级评论ID）。
 * 查询时一次性查出该内容下所有评论，在内存中组装为树形结构。
 */
@Service
public class CommentServiceImple implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Override
    public CommentDTO createComment(Long contentId, CreateCommentVO vo) {
        Comment comment = new Comment();
        comment.setContentId(contentId);
        comment.setAuthorId(vo.getAuthorId());
        comment.setContent(vo.getContent());
        comment.setParentId(vo.getParentId() != null && vo.getParentId() > 0 ? vo.getParentId() : 0L);
        comment.setCreateTime(System.currentTimeMillis());
        comment.setUpdateTime(System.currentTimeMillis());

        commentMapper.insert(comment);
        return convertToDTO(comment);
    }

    @Override
    public List<CommentDTO> listComments(Long contentId) {
        // 查出该内容下所有评论，按创建时间升序
        List<Comment> all = commentMapper.selectList(
                new QueryWrapper<Comment>()
                        .eq("content_id", contentId)
                        .orderByAsc("create_time"));

        // 转为DTO并按ID索引
        Map<Long, CommentDTO> dtoMap = new HashMap<>();
        List<CommentDTO> roots = new ArrayList<>();
        for (Comment c : all) {
            CommentDTO dto = convertToDTO(c);
            dto.setChildren(new ArrayList<>());
            dtoMap.put(dto.getId(), dto);
        }

        // 组装树形结构
        for (CommentDTO dto : dtoMap.values()) {
            if (dto.getParentId() == null || dto.getParentId() == 0L) {
                roots.add(dto);
            } else {
                CommentDTO parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    // 父评论不存在（可能已被删除），作为顶级评论展示
                    roots.add(dto);
                }
            }
        }
        return roots;
    }

    @Override
    public boolean deleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }
        // 删除该评论的所有子回复
        commentMapper.delete(new QueryWrapper<Comment>().eq("parent_id", commentId));
        // 删除评论本身
        commentMapper.deleteById(commentId);
        return true;
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContentId(comment.getContentId());
        dto.setAuthorId(comment.getAuthorId());
        dto.setContent(comment.getContent());
        dto.setParentId(comment.getParentId());
        dto.setCreateTime(comment.getCreateTime());
        dto.setUpdateTime(comment.getUpdateTime());
        return dto;
    }
}
