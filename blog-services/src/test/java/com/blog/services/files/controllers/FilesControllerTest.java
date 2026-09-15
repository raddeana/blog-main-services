package com.blog.services.files.controllers;

import com.blog.common.Result;
import com.blog.services.files.models.dto.FileDTO;
import com.blog.services.files.models.vo.UpdateFileVO;
import com.blog.services.files.services.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * FilesController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class FilesControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FilesController controller;

    // ==================== uploadFile ====================

    @Test
    void uploadFile_validFile_returns200() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hi".getBytes());

        FileDTO dto = new FileDTO();
        dto.setId(1L);
        dto.setOriginalName("test.txt");
        when(fileService.uploadFile(any(), eq(1L))).thenReturn(dto);

        Result<FileDTO> result = controller.uploadFile(file, 1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).contains("上传成功");
    }

    @Test
    void uploadFile_emptyFile_returns400() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        Result<FileDTO> result = controller.uploadFile(file, 1L);

        assertThat(result.getCode()).isEqualTo(400);
        verify(fileService, never()).uploadFile(any(), any());
    }

    @Test
    void uploadFile_illegalArgument_returns400() {
        MockMultipartFile file = new MockMultipartFile("file", "hack.exe", "application/octet-stream", "x".getBytes());
        when(fileService.uploadFile(any(), any()))
                .thenThrow(new IllegalArgumentException("不支持的文件类型: exe"));

        Result<FileDTO> result = controller.uploadFile(file, null);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("不支持的文件类型");
    }

    // ==================== listFiles ====================

    @Test
    void listFiles_returns200() {
        when(fileService.listFiles()).thenReturn(Collections.emptyList());

        Result<List<FileDTO>> result = controller.listFiles();

        assertThat(result.getCode()).isEqualTo(200);
    }

    // ==================== getFileById ====================

    @Test
    void getFileById_existing_returns200() {
        FileDTO dto = new FileDTO();
        dto.setId(1L);
        when(fileService.getFileById(1L)).thenReturn(dto);

        Result<FileDTO> result = controller.getFileById(1L);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    void getFileById_notFound_returns404() {
        when(fileService.getFileById(999L)).thenReturn(null);

        Result<FileDTO> result = controller.getFileById(999L);

        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void getFileById_invalidId_returns400() {
        Result<FileDTO> result = controller.getFileById(0L);

        assertThat(result.getCode()).isEqualTo(400);
        verify(fileService, never()).getFileById(any());
    }

    // ==================== updateFile ====================

    @Test
    void updateFile_validInput_returns200() {
        UpdateFileVO vo = new UpdateFileVO();
        vo.setOriginalName("new-name");

        FileDTO dto = new FileDTO();
        dto.setId(1L);
        when(fileService.updateFile(eq(1L), any())).thenReturn(dto);

        Result<FileDTO> result = controller.updateFile(1L, vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).contains("更新成功");
    }

    @Test
    void updateFile_notFound_returns404() {
        when(fileService.updateFile(eq(999L), any())).thenReturn(null);

        Result<FileDTO> result = controller.updateFile(999L, new UpdateFileVO());

        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void updateFile_nullBody_returns400() {
        Result<FileDTO> result = controller.updateFile(1L, null);

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void updateFile_invalidId_returns400() {
        Result<FileDTO> result = controller.updateFile(-1L, new UpdateFileVO());

        assertThat(result.getCode()).isEqualTo(400);
    }

    // ==================== deleteFile ====================

    @Test
    void deleteFile_existing_returns200() {
        FileDTO existing = new FileDTO();
        existing.setId(1L);
        when(fileService.getFileById(1L)).thenReturn(existing);
        when(fileService.deleteFile(1L)).thenReturn(true);

        Result<Void> result = controller.deleteFile(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).contains("删除成功");
    }

    @Test
    void deleteFile_notFound_returns404() {
        when(fileService.getFileById(999L)).thenReturn(null);

        Result<Void> result = controller.deleteFile(999L);

        assertThat(result.getCode()).isEqualTo(404);
        verify(fileService, never()).deleteFile(any());
    }

    @Test
    void deleteFile_invalidId_returns400() {
        Result<Void> result = controller.deleteFile(0L);

        assertThat(result.getCode()).isEqualTo(400);
    }
}
