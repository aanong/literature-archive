package com.literature.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.literature.content.entity.Chapter;
import com.literature.content.mapper.ChapterMapper;
import com.literature.content.service.ChapterService;
import org.springframework.stereotype.Service;

@Service
public class ChapterServiceImpl extends ServiceImpl<ChapterMapper, Chapter> implements ChapterService {
}
