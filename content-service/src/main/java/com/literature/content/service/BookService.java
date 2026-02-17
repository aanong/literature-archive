package com.literature.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.literature.content.entity.Book;

public interface BookService extends IService<Book> {
    /**
     * 从文本导入章节
     *
     * @param bookId  书籍ID
     * @param content 文本内容
     */
    void importChapters(Long bookId, String content);
}
