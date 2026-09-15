package com.blog.common.client;

import com.blog.common.Result;
import com.blog.common.dto.CommentDTO;
import com.blog.common.vo.CreateCommentVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论服务 Feign Client
 * <p>
 * 其他微服务通过此接口调用 blog-services 的评论相关 API
 */
@FeignClient(name = "blog-services", path = "/api/contents/{contentId}/comments")
public interface CommentClient {

    /**
     * 查询评论列表（树形结构，含子回复）
     */
    @GetMapping
    Result<List<CommentDTO>> list(@PathVariable("contentId") Long contentId);

    /**
     * 发表评论/回复
     */
    @PostMapping
    Result<CommentDTO> create(@PathVariable("contentId") Long contentId, @RequestBody CreateCommentVO vo);

    /**
     * 删除评论（级联删除子回复）
     */
    @DeleteMapping("/{id}")
    Result<Void> delete(@PathVariable("contentId") Long contentId, @PathVariable("id") Long id);
}
