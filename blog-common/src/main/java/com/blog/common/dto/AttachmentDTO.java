package com.blog.common.dto;

import java.io.Serializable;

/**
 * 内容附件数据传输对象
 */
public class AttachmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关联记录ID */
    private Long id;

    /** 内容ID */
    private Long contentId;

    /** 文件ID */
    private Long fileId;

    /** 原始文件名 */
    private String originalName;

    /** 文件大小（字节） */
    private Long size;

    /** MIME类型 */
    private String contentType;

    /** 文件扩展名 */
    private String extension;

    /** 下载地址 */
    private String downloadUrl;

    /** 关联创建时间 */
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

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
