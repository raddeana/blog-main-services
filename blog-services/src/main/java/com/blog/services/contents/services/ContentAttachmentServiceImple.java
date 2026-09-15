package com.blog.services.contents.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.services.contents.mapper.ContentAttachmentMapper;
import com.blog.services.contents.models.ContentAttachment;
import com.blog.services.contents.models.dto.AttachmentDTO;
import com.blog.services.files.models.dto.FileDTO;
import com.blog.services.files.services.FileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容附件服务实现类
 *
 * 附件上传复用 {@link FileService} 完成文件落盘与元数据持久化，
 * 再在 content_attachments 表中建立内容与文件的关联关系。
 */
@Service
public class ContentAttachmentServiceImple implements ContentAttachmentService {

    @Resource
    private ContentAttachmentMapper attachmentMapper;

    @Resource
    private FileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttachmentDTO uploadAttachment(Long contentId, MultipartFile file, Long uploaderId) {
        // 1. 复用文件服务上传文件
        FileDTO fileDTO = fileService.uploadFile(file, uploaderId);

        // 2. 建立内容-文件关联
        ContentAttachment attachment = new ContentAttachment();
        attachment.setContentId(contentId);
        attachment.setFileId(fileDTO.getId());
        attachment.setCreateTime(System.currentTimeMillis());
        attachmentMapper.insert(attachment);

        return convertToDTO(attachment, fileDTO);
    }

    @Override
    public List<AttachmentDTO> listAttachments(Long contentId) {
        List<ContentAttachment> attachments = attachmentMapper.selectList(
                new QueryWrapper<ContentAttachment>()
                        .eq("content_id", contentId)
                        .orderByAsc("create_time"));

        List<AttachmentDTO> result = new ArrayList<>();
        for (ContentAttachment attachment : attachments) {
            FileDTO fileDTO = fileService.getFileById(attachment.getFileId());
            if (fileDTO != null) {
                result.add(convertToDTO(attachment, fileDTO));
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAttachment(Long contentId, Long fileId) {
        // 删除关联记录
        int deleted = attachmentMapper.delete(
                new QueryWrapper<ContentAttachment>()
                        .eq("content_id", contentId)
                        .eq("file_id", fileId));
        if (deleted == 0) {
            return false;
        }
        // 删除文件记录与物理文件
        fileService.deleteFile(fileId);
        return true;
    }

    private AttachmentDTO convertToDTO(ContentAttachment attachment, FileDTO fileDTO) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.setId(attachment.getId());
        dto.setContentId(attachment.getContentId());
        dto.setFileId(fileDTO.getId());
        dto.setOriginalName(fileDTO.getOriginalName());
        dto.setSize(fileDTO.getSize());
        dto.setContentType(fileDTO.getContentType());
        dto.setExtension(fileDTO.getExtension());
        dto.setDownloadUrl(fileDTO.getUrl());
        dto.setCreateTime(attachment.getCreateTime());
        return dto;
    }
}
