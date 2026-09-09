package com.blog.services.contents.controllers;

import com.blog.services.common.Result;
import com.blog.services.contents.models.dto.ContentDTO;
import com.blog.services.contents.models.vo.CreateContentVO;
import com.blog.services.contents.services.ContentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 内容管理控制器（博客文章）
 *
 * RESTful API设计：
 * GET    /api/contents          查询内容列表
 * GET    /api/contents/{id}     查询单个内容
 * POST   /api/contents          创建内容
 * PUT    /api/contents/{id}     更新内容
 * DELETE /api/contents/{id}     删除内容
 */
@RestController
@RequestMapping("/api/contents")
public class ContentsController {

    @Resource
    private ContentService contentService;

    /**
     * 查询内容列表
     */
    @GetMapping
    public Result<List<ContentDTO>> listContents() {
        List<ContentDTO> contents = contentService.listContents();
        return Result.success(contents);
    }

    /**
     * 根据ID查询内容
     */
    @GetMapping("/{id}")
    public Result<ContentDTO> getContentById(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return Result.badRequest("内容ID无效");
        }
        ContentDTO content = contentService.getContentById(id);
        if (content == null) {
            return Result.notFound("内容不存在，ID: " + id);
        }
        return Result.success(content);
    }

    /**
     * 创建内容
     */
    @PostMapping
    public Result<ContentDTO> createContent(@RequestBody CreateContentVO vo) {
        if (vo == null) {
            return Result.badRequest("请求体不能为空");
        }
        if (StringUtils.isBlank(vo.getTitle())) {
            return Result.badRequest("标题不能为空");
        }
        if (StringUtils.isBlank(vo.getContent())) {
            return Result.badRequest("内容正文不能为空");
        }
        if (vo.getAuthorId() == null || vo.getAuthorId() <= 0) {
            return Result.badRequest("作者ID无效");
        }
        try {
            ContentDTO content = contentService.createContent(vo);
            return Result.success("内容创建成功", content);
        } catch (Exception e) {
            return Result.error("内容创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新内容
     */
    @PutMapping("/{id}")
    public Result<ContentDTO> updateContent(
            @PathVariable("id") Long id,
            @RequestBody CreateContentVO vo) {
        if (id == null || id <= 0) {
            return Result.badRequest("内容ID无效");
        }
        if (vo == null) {
            return Result.badRequest("请求体不能为空");
        }
        try {
            ContentDTO content = contentService.updateContent(id, vo);
            if (content == null) {
                return Result.notFound("内容不存在，ID: " + id);
            }
            return Result.success("内容更新成功", content);
        } catch (Exception e) {
            return Result.error("内容更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除内容
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteContent(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return Result.badRequest("内容ID无效");
        }
        ContentDTO existing = contentService.getContentById(id);
        if (existing == null) {
            return Result.notFound("内容不存在，ID: " + id);
        }
        try {
            contentService.deleteContent(id);
            return Result.success("内容删除成功", null);
        } catch (Exception e) {
            return Result.error("内容删除失败：" + e.getMessage());
        }
    }
}
