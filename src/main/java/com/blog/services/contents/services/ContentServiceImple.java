package com.blog.services.contents.services;

import com.blog.services.contents.models.Content;
import com.blog.services.contents.models.dto.ContentDTO;
import com.blog.services.contents.models.vo.CreateContentVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内容服务实现类
 */
@Service
public class ContentServiceImple implements ContentService {

    private final Map<Long, Content> contentRepository = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public ContentDTO createContent(CreateContentVO vo) {
        Content content = new Content();
        content.setId(idGenerator.getAndIncrement());
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
        contentRepository.put(content.getId(), content);
        return convertToDTO(content);
    }

    @Override
    public ContentDTO getContentById(Long id) {
        Content content = contentRepository.get(id);
        return content != null ? convertToDTO(content) : null;
    }

    @Override
    public List<ContentDTO> listContents() {
        List<ContentDTO> result = new ArrayList<>();
        for (Content content : contentRepository.values()) {
            result.add(convertToDTO(content));
        }
        return result;
    }

    @Override
    public ContentDTO updateContent(Long id, CreateContentVO vo) {
        Content content = contentRepository.get(id);
        if (content == null) {
            return null;
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
        return convertToDTO(content);
    }

    @Override
    public void deleteContent(Long id) {
        contentRepository.remove(id);
    }

    private ContentDTO convertToDTO(Content content) {
        ContentDTO dto = new ContentDTO();
        dto.setId(content.getId());
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
