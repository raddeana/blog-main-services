package com.blog.services.contents.controllers;

import com.blog.common.Result;
import com.blog.services.contents.models.dto.AttachmentDTO;
import com.blog.services.contents.services.ContentAttachmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 内容附件控制器
 *
 * RESTful API设计：
 * GET    /api/contents/{contentId}/attachments           查询某内容的附件列表
 * POST   /api/contents/{contentId}/attachments           上传附件（multipart/form-data）
 * DELETE /api/contents/{contentId}/attachments/{fileId}  删除附件
 */
@RestController
@RequestMapping("/api/contents/{contentId}/attachments")
public class ContentAttachmentsController {

    @Resource
    private ContentAttachmentService attachmentService;

    /**
     * 查询某内容的附件列表
     */
    @GetMapping
    public Result<List<AttachmentDTO>> listAttachments(@PathVariable("contentId") Long contentId) {
        if (contentId == null || contentId <= 0) {
            return Result.badRequest("内容ID无效");
        }
        List<AttachmentDTO> attachments = attachmentService.listAttachments(contentId);
        return Result.success(attachments);
    }

    /**
     * 上传附件
     *
     * @param contentId  内容ID
     * @param file       文件（multipart/form-data，参数名 file）
     * @param uploaderId 上传者ID（可选）
     */
    @PostMapping
    public Result<AttachmentDTO> uploadAttachment(
            @PathVariable("contentId") Long contentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        if (contentId == null || contentId <= 0) {
            return Result.badRequest("内容ID无效");
        }
        if (file == null || file.isEmpty()) {
            return Result.badRequest("上传文件不能为空");
        }
        try {
            AttachmentDTO attachment = attachmentService.uploadAttachment(contentId, file, uploaderId);
            return Result.success("附件上传成功", attachment);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("附件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除附件（同时删除文件记录与物理文件）
     */
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteAttachment(
            @PathVariable("contentId") Long contentId,
            @PathVariable("fileId") Long fileId) {
        if (contentId == null || contentId <= 0) {
            return Result.badRequest("内容ID无效");
        }
        if (fileId == null || fileId <= 0) {
            return Result.badRequest("文件ID无效");
        }
        try {
            boolean success = attachmentService.deleteAttachment(contentId, fileId);
            if (!success) {
                return Result.notFound("附件不存在，内容ID: " + contentId + "，文件ID: " + fileId);
            }
            return Result.success("附件删除成功", null);
        } catch (Exception e) {
            return Result.error("附件删除失败：" + e.getMessage());
        }
    }
}
