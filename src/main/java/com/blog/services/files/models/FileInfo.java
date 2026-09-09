package com.blog.services.files.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * 文件元信息实体
 */
@TableName("files")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 原始文件名 */
    private String originalName;

    /** 存储后的文件名（UUID，避免冲突） */
    private String storedName;

    /** 文件存储路径（相对于上传根目录的相对路径） */
    private String relativePath;

    /** 文件大小（字节） */
    private Long size;

    /** 文件MIME类型 */
    private String contentType;

    /** 文件扩展名 */
    private String extension;

    /** 上传者ID */
    private Long uploaderId;

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

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
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

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
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
