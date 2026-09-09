package com.blog.services.files.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.services.files.models.FileInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件 Mapper 接口
 */
@Mapper
public interface FileMapper extends BaseMapper<FileInfo> {
}
