package com.blog.services.contents.models.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 评论数据传输对象（含回复列表）
 */
public class CommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long contentId;
    private Long authorId;
    private String content;
    private Long parentId;
    private Long createTime;
    private Long updateTime;

    /** 子回复列表 */
    private List<CommentDTO> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public List<CommentDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CommentDTO> children) {
        this.children = children;
    }
}
