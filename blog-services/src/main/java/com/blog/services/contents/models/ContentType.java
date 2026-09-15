package com.blog.services.contents.models;

/**
 * 内容类型常量
 */
public final class ContentType {

    private ContentType() {
    }

    /** 文章 */
    public static final int ARTICLE = 1;

    /** 投票 */
    public static final int VOTE = 2;

    /** 提问 */
    public static final int QUESTION = 3;

    /**
     * 判断类型值是否合法
     */
    public static boolean isValid(Integer type) {
        return type != null && type >= ARTICLE && type <= QUESTION;
    }
}
