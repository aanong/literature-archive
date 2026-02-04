package com.literature.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.literature.content.entity.Book;
import com.literature.content.mapper.BookMapper;
import com.literature.content.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {
}
