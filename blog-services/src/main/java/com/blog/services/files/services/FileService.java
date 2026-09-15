package com.blog.services.files.services;

import com.blog.services.files.models.dto.FileDTO;
import com.blog.services.files.models.vo.UpdateFileVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file       上传的文件
     * @param uploaderId 上传者ID
     * @return 文件信息DTO
     */
    FileDTO uploadFile(MultipartFile file, Long uploaderId);

    /**
     * 根据ID查询文件信息
     *
     * @param id 文件ID
     * @return 文件信息DTO
     */
    FileDTO getFileById(Long id);

    /**
     * 查询所有文件列表
     *
     * @return 文件信息列表
     */
    List<FileDTO> listFiles();

    /**
     * 更新文件信息（如重命名）
     *
     * @param id 文件ID
     * @param vo 更新请求
     * @return 更新后的文件信息DTO
     */
    FileDTO updateFile(Long id, UpdateFileVO vo);

    /**
     * 删除文件（元数据 + 物理对象）
     *
     * @param id 文件ID
     * @return 是否删除成功
     */
    boolean deleteFile(Long id);

    /**
     * 加载文件资源用于下载
     *
     * @param id 文件ID
     * @return 文件资源，不存在返回null
     */
    Resource loadFileForDownload(Long id);
}
