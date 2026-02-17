package com.literature.common.core.dto;

/**
 * 书籍信息 DTO，用于服务间调用
 */
public record BookDTO(
        Long id,
        String title,
        String author,
        String edition,
        String status,
        String category,
        String tags) {
    /**
     * 创建默认的降级书籍信息
     */
    public static BookDTO defaultBook(Long bookId) {
        return new BookDTO(
                bookId,
                "未知书籍",
                "未知作者",
                null,
                "UNKNOWN",
                null,
                null
        );
    }
}
