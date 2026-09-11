package com.blog.services.contents.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ContentType 常量类单元测试
 */
class ContentTypeTest {

    @Test
    void isValid_null_returnsFalse() {
        assertThat(ContentType.isValid(null)).isFalse();
    }

    @Test
    void isValid_zero_returnsFalse() {
        assertThat(ContentType.isValid(0)).isFalse();
    }

    @Test
    void isValid_article_returnsTrue() {
        assertThat(ContentType.isValid(ContentType.ARTICLE)).isTrue();
    }

    @Test
    void isValid_vote_returnsTrue() {
        assertThat(ContentType.isValid(ContentType.VOTE)).isTrue();
    }

    @Test
    void isValid_question_returnsTrue() {
        assertThat(ContentType.isValid(ContentType.QUESTION)).isTrue();
    }

    @Test
    void isValid_outOfRange_returnsFalse() {
        assertThat(ContentType.isValid(4)).isFalse();
        assertThat(ContentType.isValid(-1)).isFalse();
    }
}
