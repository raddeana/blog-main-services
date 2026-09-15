package com.blog.common.client;

import com.blog.common.Result;
import com.blog.common.dto.FileDTO;
import com.blog.common.vo.UpdateFileVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务 Feign Client
 * <p>
 * 其他微服务通过此接口调用 blog-services 的文件管理 API
 */
@FeignClient(name = "blog-services", path = "/api/files")
public interface FileClient {

    /**
     * 上传文件
     */
    @PostMapping(consumes = "multipart/form-data")
    Result<FileDTO> upload(@RequestPart("file") MultipartFile file,
                           @RequestParam(value = "uploaderId", required = false) Long uploaderId);

    /**
     * 查询文件列表
     */
    @GetMapping
    Result<List<FileDTO>> list();

    /**
     * 查询单个文件元信息
     */
    @GetMapping("/{id}")
    Result<FileDTO> getById(@PathVariable("id") Long id);

    /**
     * 下载文件
     */
    @GetMapping("/{id}/download")
    ResponseEntity<Resource> download(@PathVariable("id") Long id);

    /**
     * 更新文件名
     */
    @PutMapping("/{id}")
    Result<FileDTO> update(@PathVariable("id") Long id, @RequestBody UpdateFileVO vo);

    /**
     * 删除文件（元数据 + 物理文件）
     */
    @DeleteMapping("/{id}")
    Result<Void> delete(@PathVariable("id") Long id);
}
