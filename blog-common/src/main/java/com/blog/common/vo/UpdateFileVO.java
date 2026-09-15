package com.blog.common.vo;

import java.io.Serializable;

/**
 * 更新文件信息请求VO
 */
public class UpdateFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 新的文件名（不含扩展名） */
    private String originalName;

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
}
