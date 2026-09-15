package com.blog.services.files.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.services.files.mapper.FileMapper;
import com.blog.services.files.models.FileInfo;
import com.blog.services.files.models.dto.FileDTO;
import com.blog.services.files.models.vo.UpdateFileVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 文件服务实现类
 *
 * 存储策略：
 * - 元数据持久化到 MySQL（MyBatis-Plus）
 * - 物理文件落盘到本地目录
 *
 * 安全策略：
 * 1. 使用UUID重命名存储，避免文件名冲突与覆盖攻击
 * 2. 校验文件大小与扩展名白名单
 * 3. 严格校验路径在根目录内，防止路径穿越
 */
@Service
public class FileServiceImple implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImple.class);

    @Resource
    private FileMapper fileMapper;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.max-size:52428800}")
    private long maxFileSize;

    @Value("${file.allowed-types:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar,md}")
    private String allowedTypes;

    /**
     * 获取并初始化本地上传根目录（绝对路径）
     */
    private Path getUploadRootPath() {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + root, e);
        }
        return root;
    }

    private Set<String> getAllowedExtensions() {
        Set<String> set = new HashSet<>();
        if (StringUtils.isNotBlank(allowedTypes)) {
            Arrays.stream(allowedTypes.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .forEach(set::add);
        }
        return set;
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    private String sanitizeFileName(String filename) {
        if (filename == null) {
            return "unnamed";
        }
        return filename.replaceAll("[/\\\\\\x00]", "_");
    }

    @Override
    public FileDTO uploadFile(MultipartFile file, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超出限制，最大允许 " + maxFileSize + " 字节");
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String extension = extractExtension(originalName);

        Set<String> allowed = getAllowedExtensions();
        if (!allowed.isEmpty() && !allowed.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        String storedName = UUID.randomUUID().toString().replace("-", "")
                + (StringUtils.isNotBlank(extension) ? "." + extension : "");

        // 落盘到本地
        Path rootPath = getUploadRootPath();
        Path targetPath = rootPath.resolve(storedName).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new SecurityException("非法的文件存储路径");
        }
        try {
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件存储失败: " + e.getMessage(), e);
        }

        // 持久化元数据到数据库
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalName(originalName);
        fileInfo.setStoredName(storedName);
        fileInfo.setRelativePath(storedName);
        fileInfo.setSize(file.getSize());
        fileInfo.setContentType(file.getContentType());
        fileInfo.setExtension(extension);
        fileInfo.setUploaderId(uploaderId);
        fileInfo.setCreateTime(System.currentTimeMillis());
        fileInfo.setUpdateTime(System.currentTimeMillis());

        fileMapper.insert(fileInfo);
        log.info("文件上传成功: id={}, name={}, size={}",
                fileInfo.getId(), originalName, file.getSize());
        return convertToDTO(fileInfo);
    }

    @Override
    public FileDTO getFileById(Long id) {
        FileInfo fileInfo = fileMapper.selectById(id);
        return fileInfo != null ? convertToDTO(fileInfo) : null;
    }

    @Override
    public List<FileDTO> listFiles() {
        List<FileInfo> list = fileMapper.selectList(
                new QueryWrapper<FileInfo>().orderByDesc("create_time"));
        List<FileDTO> result = new ArrayList<>();
        for (FileInfo fileInfo : list) {
            result.add(convertToDTO(fileInfo));
        }
        return result;
    }

    @Override
    public FileDTO updateFile(Long id, UpdateFileVO vo) {
        FileInfo fileInfo = fileMapper.selectById(id);
        if (fileInfo == null) {
            return null;
        }
        if (vo != null && StringUtils.isNotBlank(vo.getOriginalName())) {
            String newName = sanitizeFileName(vo.getOriginalName());
            String ext = fileInfo.getExtension();
            if (StringUtils.isNotBlank(ext) && !newName.toLowerCase().endsWith("." + ext)) {
                newName = newName + "." + ext;
            }
            fileInfo.setOriginalName(newName);
        }
        fileInfo.setUpdateTime(System.currentTimeMillis());
        fileMapper.updateById(fileInfo);
        return convertToDTO(fileInfo);
    }

    @Override
    public boolean deleteFile(Long id) {
        FileInfo fileInfo = fileMapper.selectById(id);
        if (fileInfo == null) {
            return false;
        }

        // 删除物理文件
        Path rootPath = getUploadRootPath();
        Path targetPath = rootPath.resolve(fileInfo.getStoredName()).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new SecurityException("非法的文件删除路径");
        }
        File physicalFile = targetPath.toFile();
        if (physicalFile.exists() && !physicalFile.delete()) {
            log.warn("物理文件删除失败（继续删除数据库记录）: {}", targetPath);
        }

        // 删除数据库记录
        fileMapper.deleteById(id);
        log.info("文件删除成功: id={}, name={}", id, fileInfo.getOriginalName());
        return true;
    }

    @Override
    public org.springframework.core.io.Resource loadFileForDownload(Long id) {
        FileInfo fileInfo = fileMapper.selectById(id);
        if (fileInfo == null) {
            return null;
        }

        Path rootPath = getUploadRootPath();
        Path targetPath = rootPath.resolve(fileInfo.getStoredName()).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new SecurityException("非法的文件访问路径");
        }
        File file = targetPath.toFile();
        return file.exists() ? new org.springframework.core.io.FileSystemResource(file) : null;
    }

    private FileDTO convertToDTO(FileInfo fileInfo) {
        FileDTO dto = new FileDTO();
        dto.setId(fileInfo.getId());
        dto.setOriginalName(fileInfo.getOriginalName());
        dto.setStoredName(fileInfo.getStoredName());
        dto.setSize(fileInfo.getSize());
        dto.setContentType(fileInfo.getContentType());
        dto.setExtension(fileInfo.getExtension());
        dto.setUrl("/api/files/" + fileInfo.getId() + "/download");
        dto.setUploaderId(fileInfo.getUploaderId());
        dto.setCreateTime(fileInfo.getCreateTime());
        dto.setUpdateTime(fileInfo.getUpdateTime());
        return dto;
    }
}
