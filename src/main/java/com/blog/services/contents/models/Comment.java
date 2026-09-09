package com.blog.services.contents.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * 内容评论实体
 */
@TableName("comments")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 内容ID */
    private Long contentId;

    /** 评论者ID */
    private Long authorId;

    /** 评论内容 */
    private String content;

    /** 父评论ID（0表示顶级评论） */
    private Long parentId;

    /** 创建时间（毫秒时间戳） */
    private Long createTime;

    /** 更新时间（毫秒时间戳） */
    private Long updateTime;

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
}
