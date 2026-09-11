package com.blog.services.contents.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.services.contents.mapper.CommentMapper;
import com.blog.services.contents.mapper.ContentAttachmentMapper;
import com.blog.services.contents.mapper.ContentMapper;
import com.blog.services.contents.models.Content;
import com.blog.services.contents.models.ContentAttachment;
import com.blog.services.contents.models.ContentType;
import com.blog.services.contents.models.dto.ContentDTO;
import com.blog.services.contents.models.vo.CreateContentVO;
import com.blog.services.files.services.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ContentServiceImple 单元测试
 * <p>
 * 使用 Mockito 模拟 Mapper 和 FileService，无需 MySQL 实例。
 */
@ExtendWith(MockitoExtension.class)
class ContentServiceImpleTest {

    @Mock
    private ContentMapper contentMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private ContentAttachmentMapper attachmentMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ContentServiceImple contentService;

    // ==================== createContent ====================

    @Test
    void createContent_validInput_shouldInsertAndReturnDTO() {
        CreateContentVO vo = new CreateContentVO();
        vo.setType(ContentType.VOTE);
        vo.setTitle("测试投票");
        vo.setContent("投票正文");
        vo.setAuthorId(1L);
        vo.setCategory("技术");
        vo.setTags("java,spring");

        // 模拟 insert 后自增 ID 回填
        doAnswer(invocation -> {
            Content c = invocation.getArgument(0);
            c.setId(10L);
            return 1;
        }).when(contentMapper).insert(any(Content.class));

        ContentDTO result = contentService.createContent(vo);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getType()).isEqualTo(ContentType.VOTE);
        assertThat(result.getTitle()).isEqualTo("测试投票");
        assertThat(result.getAuthorId()).isEqualTo(1L);
        assertThat(result.getCategory()).isEqualTo("技术");
        assertThat(result.getTags()).isEqualTo("java,spring");
        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getViewCount()).isEqualTo(0L);
        assertThat(result.getLikeCount()).isEqualTo(0L);
        assertThat(result.getCommentCount()).isEqualTo(0L);
        assertThat(result.getCreateTime()).isNotNull();
        assertThat(result.getUpdateTime()).isNotNull();
    }

    @Test
    void createContent_nullType_defaultsToArticle() {
        CreateContentVO vo = new CreateContentVO();
        vo.setTitle("无类型");
        vo.setContent("正文");
        vo.setAuthorId(1L);

        doAnswer(invocation -> {
            Content c = invocation.getArgument(0);
            c.setId(1L);
            return 1;
        }).when(contentMapper).insert(any(Content.class));

        ContentDTO result = contentService.createContent(vo);

        assertThat(result.getType()).isEqualTo(ContentType.ARTICLE);
    }

    @Test
    void createContent_invalidType_defaultsToArticle() {
        CreateContentVO vo = new CreateContentVO();
        vo.setType(99);
        vo.setTitle("非法类型");
        vo.setContent("正文");
        vo.setAuthorId(1L);

        doAnswer(invocation -> {
            Content c = invocation.getArgument(0);
            c.setId(1L);
            return 1;
        }).when(contentMapper).insert(any(Content.class));

        ContentDTO result = contentService.createContent(vo);

        assertThat(result.getType()).isEqualTo(ContentType.ARTICLE);
    }

    // ==================== getContentById ====================

    @Test
    void getContentById_existing_returnsDTO() {
        Content content = buildContent(1L, ContentType.ARTICLE, "标题", "正文", 1L);
        when(contentMapper.selectById(1L)).thenReturn(content);

        ContentDTO result = contentService.getContentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("标题");
    }

    @Test
    void getContentById_notFound_returnsNull() {
        when(contentMapper.selectById(999L)).thenReturn(null);

        ContentDTO result = contentService.getContentById(999L);

        assertThat(result).isNull();
    }

    // ==================== listContents ====================

    @Test
    @SuppressWarnings("unchecked")
    void listContents_withTypeFilter_returnsOnlyMatching() {
        Content article = buildContent(1L, ContentType.ARTICLE, "文章", "正文A", 1L);
        Content vote = buildContent(2L, ContentType.VOTE, "投票", "正文B", 1L);

        when(contentMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Collections.singletonList(vote));

        List<ContentDTO> result = contentService.listContents(ContentType.VOTE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ContentType.VOTE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listContents_nullType_returnsAll() {
        Content c1 = buildContent(1L, ContentType.ARTICLE, "文章1", "正文1", 1L);
        Content c2 = buildContent(2L, ContentType.VOTE, "投票1", "正文2", 1L);

        when(contentMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(c1, c2));

        List<ContentDTO> result = contentService.listContents(null);

        assertThat(result).hasSize(2);
    }

    // ==================== updateContent ====================

    @Test
    void updateContent_existing_updatesFields() {
        Content existing = buildContent(1L, ContentType.ARTICLE, "旧标题", "旧正文", 1L);
        when(contentMapper.selectById(1L)).thenReturn(existing);

        CreateContentVO vo = new CreateContentVO();
        vo.setTitle("新标题");
        vo.setSummary("新摘要");

        ContentDTO result = contentService.updateContent(1L, vo);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("新标题");
        assertThat(result.getSummary()).isEqualTo("新摘要");
        verify(contentMapper).updateById(any(Content.class));
    }

    @Test
    void updateContent_notFound_returnsNull() {
        when(contentMapper.selectById(999L)).thenReturn(null);

        ContentDTO result = contentService.updateContent(999L, new CreateContentVO());

        assertThat(result).isNull();
        verify(contentMapper, never()).updateById(any(Content.class));
    }

    // ==================== deleteContent ====================

    @Test
    void deleteContent_cascadesCommentsAttachmentsAndFiles() {
        Long contentId = 1L;

        // 模拟该内容下有 2 个附件
        ContentAttachment att1 = new ContentAttachment();
        att1.setId(1L);
        att1.setContentId(contentId);
        att1.setFileId(100L);

        ContentAttachment att2 = new ContentAttachment();
        att2.setId(2L);
        att2.setContentId(contentId);
        att2.setFileId(200L);

        when(attachmentMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(att1, att2));

        contentService.deleteContent(contentId);

        // 验证删评论
        verify(commentMapper).delete(any(QueryWrapper.class));
        // 验证删附件关联
        verify(attachmentMapper).delete(any(QueryWrapper.class));
        // 验证联动删文件（2 个附件）
        verify(fileService).deleteFile(100L);
        verify(fileService).deleteFile(200L);
        // 验证删内容本身
        verify(contentMapper).deleteById(contentId);
    }

    @Test
    void deleteContent_noAttachments_stillDeletesContentAndComments() {
        when(attachmentMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        contentService.deleteContent(1L);

        verify(commentMapper).delete(any(QueryWrapper.class));
        verify(fileService, never()).deleteFile(any());
        verify(attachmentMapper).delete(any(QueryWrapper.class));
        verify(contentMapper).deleteById(1L);
    }

    // ==================== helper ====================

    private Content buildContent(Long id, int type, String title, String content, Long authorId) {
        Content c = new Content();
        c.setId(id);
        c.setType(type);
        c.setTitle(title);
        c.setContent(content);
        c.setAuthorId(authorId);
        c.setStatus(1);
        c.setViewCount(0L);
        c.setLikeCount(0L);
        c.setCommentCount(0L);
        c.setCreateTime(System.currentTimeMillis());
        c.setUpdateTime(System.currentTimeMillis());
        return c;
    }
}
