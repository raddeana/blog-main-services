package com.blog.services.contents.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.services.contents.models.Content;
import org.apache.ibatis.annotations.Mapper;

/**
 * 内容 Mapper 接口
 */
@Mapper
public interface ContentMapper extends BaseMapper<Content> {
}
