package com.blog.services.contents.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.services.contents.mapper.CommentMapper;
import com.blog.services.contents.mapper.ContentAttachmentMapper;
import com.blog.services.contents.mapper.ContentMapper;
import com.blog.services.contents.models.Content;
import com.blog.services.contents.models.ContentAttachment;
import com.blog.services.contents.models.ContentType;
import com.blog.services.contents.models.dto.ContentDTO;
import com.blog.services.contents.models.vo.CreateContentVO;
import com.blog.services.files.services.FileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容服务实现类（MySQL + MyBatis-Plus 持久化）
 */
@Service
public class ContentServiceImple implements ContentService {

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ContentAttachmentMapper attachmentMapper;

    @Resource
    private FileService fileService;

    @Override
    public ContentDTO createContent(CreateContentVO vo) {
        Content content = new Content();
        content.setType(ContentType.isValid(vo.getType()) ? vo.getType() : ContentType.ARTICLE);
        content.setTitle(vo.getTitle());
        content.setSummary(vo.getSummary());
        content.setContent(vo.getContent());
        content.setAuthorId(vo.getAuthorId());
        content.setCategory(vo.getCategory());
        content.setTags(vo.getTags());
        content.setCoverImage(vo.getCoverImage());
        content.setStatus(1);
        content.setViewCount(0L);
        content.setLikeCount(0L);
        content.setCommentCount(0L);
        content.setCreateTime(System.currentTimeMillis());
        content.setUpdateTime(System.currentTimeMillis());

        contentMapper.insert(content);
        return convertToDTO(content);
    }

    @Override
    public ContentDTO getContentById(Long id) {
        Content content = contentMapper.selectById(id);
        return content != null ? convertToDTO(content) : null;
    }

    @Override
    public List<ContentDTO> listContents(Integer type) {
        QueryWrapper<Content> wrapper = new QueryWrapper<>();
        if (type != null) {
            wrapper.eq("type", type);
        }
        wrapper.orderByDesc("create_time");

        List<ContentDTO> result = new ArrayList<>();
        for (Content content : contentMapper.selectList(wrapper)) {
            result.add(convertToDTO(content));
        }
        return result;
    }

    @Override
    public ContentDTO updateContent(Long id, CreateContentVO vo) {
        Content content = contentMapper.selectById(id);
        if (content == null) {
            return null;
        }
        if (vo.getType() != null) {
            content.setType(vo.getType());
        }
        if (vo.getTitle() != null) {
            content.setTitle(vo.getTitle());
        }
        if (vo.getSummary() != null) {
            content.setSummary(vo.getSummary());
        }
        if (vo.getContent() != null) {
            content.setContent(vo.getContent());
        }
        if (vo.getCategory() != null) {
            content.setCategory(vo.getCategory());
        }
        if (vo.getTags() != null) {
            content.setTags(vo.getTags());
        }
        if (vo.getCoverImage() != null) {
            content.setCoverImage(vo.getCoverImage());
        }
        content.setUpdateTime(System.currentTimeMillis());

        contentMapper.updateById(content);
        return convertToDTO(content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContent(Long id) {
        // 1. 删除该内容下的所有评论
        commentMapper.delete(new QueryWrapper<com.blog.services.contents.models.Comment>()
                .eq("content_id", id));

        // 2. 删除附件关联记录，并联动删除文件（元数据 + 物理文件）
        List<ContentAttachment> attachments = attachmentMapper.selectList(
                new QueryWrapper<ContentAttachment>().eq("content_id", id));
        for (ContentAttachment attachment : attachments) {
            fileService.deleteFile(attachment.getFileId());
        }
        attachmentMapper.delete(new QueryWrapper<ContentAttachment>().eq("content_id", id));

        // 3. 删除内容本身
        contentMapper.deleteById(id);
    }

    private ContentDTO convertToDTO(Content content) {
        ContentDTO dto = new ContentDTO();
        dto.setId(content.getId());
        dto.setType(content.getType());
        dto.setTitle(content.getTitle());
        dto.setSummary(content.getSummary());
        dto.setContent(content.getContent());
        dto.setAuthorId(content.getAuthorId());
        dto.setCategory(content.getCategory());
        dto.setTags(content.getTags());
        dto.setCoverImage(content.getCoverImage());
        dto.setStatus(content.getStatus());
        dto.setViewCount(content.getViewCount());
        dto.setLikeCount(content.getLikeCount());
        dto.setCommentCount(content.getCommentCount());
        dto.setCreateTime(content.getCreateTime());
        dto.setUpdateTime(content.getUpdateTime());
        return dto;
    }
}
