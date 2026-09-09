package com.blog.services.contents.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * 内容附件关联实体
 */
@TableName("content_attachments")
public class ContentAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关联ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 内容ID */
    private Long contentId;

    /** 文件ID（关联files表） */
    private Long fileId;

    /** 创建时间（毫秒时间戳） */
    private Long createTime;

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

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
