package com.blog.services.contents.controllers;

import com.blog.common.Result;
import com.blog.services.contents.models.ContentType;
import com.blog.services.contents.models.dto.ContentDTO;
import com.blog.services.contents.models.vo.CreateContentVO;
import com.blog.services.contents.services.ContentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ContentsController 单元测试
 * <p>
 * 聚焦入参校验和 Result 响应码，不涉及 Service 层实现。
 */
@ExtendWith(MockitoExtension.class)
class ContentsControllerTest {

    @Mock
    private ContentService contentService;

    @InjectMocks
    private ContentsController controller;

    // ==================== listContents ====================

    @Test
    void listContents_nullType_callsListWithNull() {
        when(contentService.listContents(null)).thenReturn(Collections.emptyList());

        Result<List<ContentDTO>> result = controller.listContents(null);

        assertThat(result.getCode()).isEqualTo(200);
        verify(contentService).listContents(null);
    }

    @Test
    void listContents_validType_callsListWithType() {
        when(contentService.listContents(ContentType.VOTE)).thenReturn(Collections.emptyList());

        Result<List<ContentDTO>> result = controller.listContents(ContentType.VOTE);

        assertThat(result.getCode()).isEqualTo(200);
        verify(contentService).listContents(ContentType.VOTE);
    }

    @Test
    void listContents_invalidType_returns400() {
        Result<List<ContentDTO>> result = controller.listContents(9);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("内容类型无效");
        verify(contentService, never()).listContents(any());
    }

    // ==================== getContentById ====================

    @Test
    void getContentById_validId_returns200() {
        ContentDTO dto = new ContentDTO();
        dto.setId(1L);
        dto.setTitle("测试");
        when(contentService.getContentById(1L)).thenReturn(dto);

        Result<ContentDTO> result = controller.getContentById(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getTitle()).isEqualTo("测试");
    }

    @Test
    void getContentById_notFound_returns404() {
        when(contentService.getContentById(999L)).thenReturn(null);

        Result<ContentDTO> result = controller.getContentById(999L);

        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void getContentById_invalidId_returns400() {
        Result<ContentDTO> result = controller.getContentById(0L);

        assertThat(result.getCode()).isEqualTo(400);
        verify(contentService, never()).getContentById(any());
    }

    // ==================== createContent ====================

    @Test
    void createContent_validInput_returns200() {
        CreateContentVO vo = new CreateContentVO();
        vo.setType(ContentType.ARTICLE);
        vo.setTitle("标题");
        vo.setContent("正文");
        vo.setAuthorId(1L);

        ContentDTO dto = new ContentDTO();
        dto.setId(1L);
        when(contentService.createContent(any(CreateContentVO.class))).thenReturn(dto);

        Result<ContentDTO> result = controller.createContent(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).contains("创建成功");
    }

    @Test
    void createContent_nullBody_returns400() {
        Result<ContentDTO> result = controller.createContent(null);

        assertThat(result.getCode()).isEqualTo(400);
        verify(contentService, never()).createContent(any());
    }

    @Test
    void createContent_blankTitle_returns400() {
        CreateContentVO vo = new CreateContentVO();
        vo.setTitle("");
        vo.setContent("正文");
        vo.setAuthorId(1L);

        Result<ContentDTO> result = controller.createContent(vo);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("标题");
    }

    @Test
    void createContent_blankContent_returns400() {
        CreateContentVO vo = new CreateContentVO();
        vo.setTitle("标题");
        vo.setContent("");
        vo.setAuthorId(1L);

        Result<ContentDTO> result = controller.createContent(vo);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("正文");
    }

    @Test
    void createContent_invalidAuthorId_returns400() {
        CreateContentVO vo = new CreateContentVO();
        vo.setTitle("标题");
        vo.setContent("正文");
        vo.setAuthorId(0L);

        Result<ContentDTO> result = controller.createContent(vo);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("作者ID");
    }

    @Test
    void createContent_invalidType_returns400() {
        CreateContentVO vo = new CreateContentVO();
        vo.setType(99);
        vo.setTitle("标题");
        vo.setContent("正文");
        vo.setAuthorId(1L);

        Result<ContentDTO> result = controller.createContent(vo);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("内容类型无效");
    }

    // ==================== updateContent ====================

    @Test
    void updateContent_validInput_returns200() {
        CreateContentVO vo = new CreateContentVO();
        vo.setTitle("新标题");

        ContentDTO dto = new ContentDTO();
        dto.setId(1L);
        dto.setTitle("新标题");
        when(contentService.updateContent(eq(1L), any(CreateContentVO.class))).thenReturn(dto);

        Result<ContentDTO> result = controller.updateContent(1L, vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).contains("更新成功");
    }

    @Test
    void updateContent_notFound_returns404() {
        when(contentService.updateContent(eq(999L), any())).thenReturn(null);

        Result<ContentDTO> result = controller.updateContent(999L, new CreateContentVO());

        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void updateContent_nullBody_returns400() {
        Result<ContentDTO> result = controller.updateContent(1L, null);

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void updateContent_invalidType_returns400() {
        CreateContentVO vo = new CreateContentVO();
        vo.setType(0);

        Result<ContentDTO> result = controller.updateContent(1L, vo);

        assertThat(result.getCode()).isEqualTo(400);
    }

    // ==================== deleteContent ====================

    @Test
    void deleteContent_existing_returns200() {
        ContentDTO existing = new ContentDTO();
        existing.setId(1L);
        when(contentService.getContentById(1L)).thenReturn(existing);

        Result<Void> result = controller.deleteContent(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).contains("删除成功");
        verify(contentService).deleteContent(1L);
    }

    @Test
    void deleteContent_notFound_returns404() {
        when(contentService.getContentById(999L)).thenReturn(null);

        Result<Void> result = controller.deleteContent(999L);

        assertThat(result.getCode()).isEqualTo(404);
        verify(contentService, never()).deleteContent(any());
    }

    @Test
    void deleteContent_invalidId_returns400() {
        Result<Void> result = controller.deleteContent(-1L);

        assertThat(result.getCode()).isEqualTo(400);
        verify(contentService, never()).deleteContent(any());
    }
}
