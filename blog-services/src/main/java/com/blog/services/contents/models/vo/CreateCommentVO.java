package com.blog.services.contents.models.vo;

import java.io.Serializable;

/**
 * 创建评论请求对象
 */
public class CreateCommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论者ID */
    private Long authorId;

    /** 评论内容 */
    private String content;

    /** 父评论ID（不传或0表示顶级评论） */
    private Long parentId;

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
}
