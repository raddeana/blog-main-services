package com.blog.services.files.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.services.files.mapper.FileMapper;
import com.blog.services.files.models.FileInfo;
import com.blog.services.files.models.dto.FileDTO;
import com.blog.services.files.models.vo.UpdateFileVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FileServiceImple 单元测试
 * <p>
 * 上传/删除涉及文件 I/O，使用 @TempDir 隔离物理文件；
 * 其余纯 Mapper 操作直接 Mock。
 */
@ExtendWith(MockitoExtension.class)
class FileServiceImpleTest {

    @Mock
    private FileMapper fileMapper;

    @InjectMocks
    private FileServiceImple fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // 注入 @Value 字段
        ReflectionTestUtils.setField(fileService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(fileService, "maxFileSize", 52428800L);
        ReflectionTestUtils.setField(fileService, "allowedTypes", "txt,jpg,png,pdf");
    }

    // ==================== uploadFile ====================

    @Test
    void uploadFile_validTxt_succeeds() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());

        doAnswer(invocation -> {
            FileInfo fi = invocation.getArgument(0);
            fi.setId(1L);
            return 1;
        }).when(fileMapper).insert(any(FileInfo.class));

        FileDTO result = fileService.uploadFile(file, 42L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOriginalName()).isEqualTo("test.txt");
        assertThat(result.getExtension()).isEqualTo("txt");
        assertThat(result.getSize()).isEqualTo(5L);
        assertThat(result.getUploaderId()).isEqualTo(42L);
        assertThat(result.getUrl()).isEqualTo("/api/files/1/download");
        assertThat(result.getStoredName()).endsWith(".txt");
    }

    @Test
    void uploadFile_emptyFile_throwsIllegalArgument() {
        MultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> fileService.uploadFile(file, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能为空");
    }

    @Test
    void uploadFile_nullFile_throwsIllegalArgument() {
        assertThatThrownBy(() -> fileService.uploadFile(null, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uploadFile_unsupportedExtension_throwsIllegalArgument() {
        MultipartFile file = new MockMultipartFile("file", "hack.exe", "application/octet-stream", "x".getBytes());

        assertThatThrownBy(() -> fileService.uploadFile(file, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的文件类型");
    }

    @Test
    void uploadFile_oversizedFile_throwsIllegalArgument() {
        ReflectionTestUtils.setField(fileService, "maxFileSize", 3L);
        byte[] content = "1234".getBytes();
        MultipartFile file = new MockMultipartFile("file", "big.txt", "text/plain", content);

        assertThatThrownBy(() -> fileService.uploadFile(file, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件大小超出限制");
    }

    // ==================== getFileById ====================

    @Test
    void getFileById_existing_returnsDTO() {
        FileInfo fi = buildFileInfo(1L, "doc.pdf", "uuid.pdf", "pdf", 1024L, 1L);
        when(fileMapper.selectById(1L)).thenReturn(fi);

        FileDTO result = fileService.getFileById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOriginalName()).isEqualTo("doc.pdf");
    }

    @Test
    void getFileById_notFound_returnsNull() {
        when(fileMapper.selectById(999L)).thenReturn(null);

        FileDTO result = fileService.getFileById(999L);

        assertThat(result).isNull();
    }

    // ==================== listFiles ====================

    @Test
    @SuppressWarnings("unchecked")
    void listFiles_returnsAll() {
        FileInfo f1 = buildFileInfo(1L, "a.txt", "u1.txt", "txt", 10L, 1L);
        FileInfo f2 = buildFileInfo(2L, "b.pdf", "u2.pdf", "pdf", 20L, 2L);

        when(fileMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(f1, f2));

        List<FileDTO> result = fileService.listFiles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOriginalName()).isEqualTo("a.txt");
        assertThat(result.get(1).getOriginalName()).isEqualTo("b.pdf");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listFiles_empty_returnsEmptyList() {
        when(fileMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<FileDTO> result = fileService.listFiles();

        assertThat(result).isEmpty();
    }

    // ==================== updateFile ====================

    @Test
    void updateFile_rename_keepsExtension() {
        FileInfo existing = buildFileInfo(1L, "old.txt", "uuid.txt", "txt", 10L, 1L);
        when(fileMapper.selectById(1L)).thenReturn(existing);

        UpdateFileVO vo = new UpdateFileVO();
        vo.setOriginalName("new-name");

        FileDTO result = fileService.updateFile(1L, vo);

        assertThat(result.getOriginalName()).isEqualTo("new-name.txt");
        verify(fileMapper).updateById(existing);
    }

    @Test
    void updateFile_renameWithExtension_doesNotDuplicate() {
        FileInfo existing = buildFileInfo(1L, "old.pdf", "uuid.pdf", "pdf", 10L, 1L);
        when(fileMapper.selectById(1L)).thenReturn(existing);

        UpdateFileVO vo = new UpdateFileVO();
        vo.setOriginalName("report.pdf");

        FileDTO result = fileService.updateFile(1L, vo);

        assertThat(result.getOriginalName()).isEqualTo("report.pdf");
    }

    @Test
    void updateFile_notFound_returnsNull() {
        when(fileMapper.selectById(999L)).thenReturn(null);

        FileDTO result = fileService.updateFile(999L, new UpdateFileVO());

        assertThat(result).isNull();
        verify(fileMapper, never()).updateById(any(FileInfo.class));
    }

    // ==================== deleteFile ====================

    @Test
    void deleteFile_existing_deletesPhysicalAndDB() throws Exception {
        FileInfo fi = buildFileInfo(1L, "test.txt", "uuid.txt", "txt", 5L, 1L);
        when(fileMapper.selectById(1L)).thenReturn(fi);

        // 先创建物理文件
        tempDir.resolve("uuid.txt").toFile().createNewFile();

        boolean result = fileService.deleteFile(1L);

        assertThat(result).isTrue();
        assertThat(tempDir.resolve("uuid.txt")).doesNotExist();
        verify(fileMapper).deleteById(1L);
    }

    @Test
    void deleteFile_notFound_returnsFalse() {
        when(fileMapper.selectById(999L)).thenReturn(null);

        boolean result = fileService.deleteFile(999L);

        assertThat(result).isFalse();
        verify(fileMapper, never()).deleteById(any());
    }

    // ==================== helper ====================

    private FileInfo buildFileInfo(Long id, String originalName, String storedName, String ext, Long size, Long uploaderId) {
        FileInfo fi = new FileInfo();
        fi.setId(id);
        fi.setOriginalName(originalName);
        fi.setStoredName(storedName);
        fi.setRelativePath(storedName);
        fi.setSize(size);
        fi.setContentType("application/octet-stream");
        fi.setExtension(ext);
        fi.setUploaderId(uploaderId);
        fi.setCreateTime(System.currentTimeMillis());
        fi.setUpdateTime(System.currentTimeMillis());
        return fi;
    }
}
