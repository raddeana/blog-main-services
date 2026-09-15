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
     * 查询内容列表
     *
     * @param type 内容类型（1-文章，2-投票，3-提问）；传null表示查询全部类型
     */
    List<ContentDTO> listContents(Integer type);

    /**
     * 更新内容
     */
    ContentDTO updateContent(Long id, CreateContentVO vo);

    /**
     * 删除内容
     */
    void deleteContent(Long id);
}
