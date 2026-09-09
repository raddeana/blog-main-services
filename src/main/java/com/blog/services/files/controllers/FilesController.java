package com.blog.services.files.controllers;

import com.blog.services.common.Result;
import com.blog.services.files.models.dto.FileDTO;
import com.blog.services.files.models.vo.UpdateFileVO;
import com.blog.services.files.services.FileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件管理控制器
 *
 * RESTful API设计：
 * POST   /api/files                  上传文件
 * GET    /api/files                  查询文件列表
 * GET    /api/files/{id}             查询单个文件元信息
 * GET    /api/files/{id}/download    下载文件
 * PUT    /api/files/{id}             更新文件信息（重命名）
 * DELETE /api/files/{id}             删除文件
 */
@RestController
@RequestMapping("/api/files")
public class FilesController {

    @Resource
    private FileService fileService;

    /**
     * 上传文件
     *
     * @param file       上传的文件（multipart/form-data）
     * @param uploaderId 上传者ID（可选）
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        if (file == null || file.isEmpty()) {
            return Result.badRequest("上传文件不能为空");
        }
        try {
            FileDTO fileDTO = fileService.uploadFile(file, uploaderId);
            return Result.success("文件上传成功", fileDTO);
        } catch (IllegalArgumentException | SecurityException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 查询文件列表
     */
    @GetMapping
    public Result<List<FileDTO>> listFiles() {
        List<FileDTO> files = fileService.listFiles();
        return Result.success(files);
    }

    /**
     * 根据ID查询文件元信息
     */
    @GetMapping("/{id}")
    public Result<FileDTO> getFileById(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return Result.badRequest("文件ID无效");
        }
        FileDTO fileDTO = fileService.getFileById(id);
        if (fileDTO == null) {
            return Result.notFound("文件不存在，ID: " + id);
        }
        return Result.success(fileDTO);
    }

    /**
     * 下载文件
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        FileDTO fileDTO = fileService.getFileById(id);
        if (fileDTO == null) {
            return ResponseEntity.notFound().build();
        }
        org.springframework.core.io.Resource resource = fileService.loadFileForDownload(id);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String encodedName = URLEncoder.encode(fileDTO.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            StringUtils.isNotBlank(fileDTO.getContentType())
                                    ? fileDTO.getContentType()
                                    : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新文件信息（重命名）
     */
    @PutMapping("/{id}")
    public Result<FileDTO> updateFile(
            @PathVariable("id") Long id,
            @RequestBody UpdateFileVO vo) {
        if (id == null || id <= 0) {
            return Result.badRequest("文件ID无效");
        }
        if (vo == null) {
            return Result.badRequest("请求体不能为空");
        }
        try {
            FileDTO fileDTO = fileService.updateFile(id, vo);
            if (fileDTO == null) {
                return Result.notFound("文件不存在，ID: " + id);
            }
            return Result.success("文件更新成功", fileDTO);
        } catch (Exception e) {
            return Result.error("文件更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return Result.badRequest("文件ID无效");
        }
        FileDTO existing = fileService.getFileById(id);
        if (existing == null) {
            return Result.notFound("文件不存在，ID: " + id);
        }
        try {
            boolean deleted = fileService.deleteFile(id);
            if (!deleted) {
                return Result.error("文件删除失败");
            }
            return Result.success("文件删除成功", null);
        } catch (Exception e) {
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }
}
