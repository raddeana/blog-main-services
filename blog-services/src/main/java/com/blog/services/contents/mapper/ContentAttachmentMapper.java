package com.blog.services.contents.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.services.contents.models.ContentAttachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 内容附件关联 Mapper 接口
 */
@Mapper
public interface ContentAttachmentMapper extends BaseMapper<ContentAttachment> {
}
