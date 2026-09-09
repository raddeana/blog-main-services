package com.blog.services.contents.services;

import com.blog.services.contents.models.dto.AttachmentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 内容附件服务接口
 */
public interface ContentAttachmentService {

    /**
     * 为内容上传附件
     *
     * @param contentId  内容ID
     * @param file       上传的文件
     * @param uploaderId 上传者ID
     * @return 附件信息
     */
    AttachmentDTO uploadAttachment(Long contentId, MultipartFile file, Long uploaderId);

    /**
     * 查询某内容的所有附件
     *
     * @param contentId 内容ID
     * @return 附件列表
     */
    List<AttachmentDTO> listAttachments(Long contentId);

    /**
     * 删除内容附件（同时删除文件记录与物理文件）
     *
     * @param contentId 内容ID
     * @param fileId    文件ID
     * @return 是否删除成功
     */
    boolean deleteAttachment(Long contentId, Long fileId);
}
