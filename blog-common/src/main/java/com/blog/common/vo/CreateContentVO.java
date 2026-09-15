package com.blog.common.vo;

import java.io.Serializable;

/**
 * 创建/更新内容请求VO
 */
public class CreateContentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 内容类型（1-文章，2-投票，3-提问）；不传默认为文章 */
    private Integer type;
    private String title;
    private String summary;
    private String content;
    private Long authorId;
    private String category;
    private String tags;
    private String coverImage;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
}
