package com.blog.common.client;

import com.blog.common.Result;
import com.blog.common.dto.AttachmentDTO;
import com.blog.common.dto.ContentDTO;
import com.blog.common.vo.CreateContentVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容服务 Feign Client
 * <p>
 * 其他微服务通过此接口调用 blog-services 的内容相关 API
 */
@FeignClient(name = "blog-services", path = "/api/contents")
public interface ContentClient {

    /**
     * 查询内容列表（可按类型筛选）
     */
    @GetMapping
    Result<List<ContentDTO>> list(@RequestParam(value = "type", required = false) Integer type);

    /**
     * 查询单个内容
     */
    @GetMapping("/{id}")
    Result<ContentDTO> getById(@PathVariable("id") Long id);

    /**
     * 创建内容
     */
    @PostMapping
    Result<ContentDTO> create(@RequestBody CreateContentVO vo);

    /**
     * 更新内容
     */
    @PutMapping("/{id}")
    Result<ContentDTO> update(@PathVariable("id") Long id, @RequestBody CreateContentVO vo);

    /**
     * 删除内容（级联删除评论和附件）
     */
    @DeleteMapping("/{id}")
    Result<Void> delete(@PathVariable("id") Long id);

    /**
     * 查询内容的附件列表
     */
    @GetMapping("/{contentId}/attachments")
    Result<List<AttachmentDTO>> listAttachments(@PathVariable("contentId") Long contentId);

    /**
     * 删除内容的附件
     */
    @DeleteMapping("/{contentId}/attachments/{fileId}")
    Result<Void> deleteAttachment(@PathVariable("contentId") Long contentId, @PathVariable("fileId") Long fileId);
}
