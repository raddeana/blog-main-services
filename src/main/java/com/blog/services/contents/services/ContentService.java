package com.blog.services.contents.services;

import com.blog.services.contents.models.dto.ContentDTO;
import com.blog.services.contents.models.vo.CreateContentVO;

import java.util.List;

/**
 * 内容服务接口
 */
public interface ContentService {

    /**
     * 创建内容
     */
    ContentDTO createContent(CreateContentVO vo);

    /**
     * 根据ID查询内容
     */
    ContentDTO getContentById(Long id);

    /**
     * 查询所有内容
     */
    List<ContentDTO> listContents();

    /**
     * 更新内容
     */
    ContentDTO updateContent(Long id, CreateContentVO vo);

    /**
     * 删除内容
     */
    void deleteContent(Long id);
}
