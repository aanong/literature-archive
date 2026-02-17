package com.literature.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.literature.content.entity.Book;
import com.literature.content.mapper.BookMapper;
import com.literature.content.service.BookService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.content.entity.Chapter;
import com.literature.content.entity.Volume;
import com.literature.content.service.ChapterService;
import com.literature.content.service.VolumeService;
import com.literature.content.util.ChapterParser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    private final VolumeService volumeService;
    private final ChapterService chapterService;

    public BookServiceImpl(VolumeService volumeService, ChapterService chapterService) {
        this.volumeService = volumeService;
        this.chapterService = chapterService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importChapters(Long bookId, String content) {
        // 1. 解析章节
        List<Chapter> chapters = ChapterParser.parse(content);
        if (chapters.isEmpty()) {
            return;
        }

        // 2. 获取或创建默认卷
        LambdaQueryWrapper<Volume> volumeWrapper = new LambdaQueryWrapper<>();
        volumeWrapper.eq(Volume::getBookId, bookId);
        volumeWrapper.orderByAsc(Volume::getOrderNo);
        List<Volume> volumes = volumeService.list(volumeWrapper);

        Long volumeId;
        if (volumes.isEmpty()) {
            Volume defaultVolume = new Volume();
            defaultVolume.setBookId(bookId);
            defaultVolume.setTitle("默认卷");
            defaultVolume.setOrderNo(1);
            defaultVolume.setStatus("PUBLISHED");
            volumeService.save(defaultVolume);
            volumeId = defaultVolume.getId();
        } else {
            volumeId = volumes.get(0).getId();
        }

        // 3. 关联卷ID并保存章节
        for (Chapter chapter : chapters) {
            chapter.setVolumeId(volumeId);
        }
        chapterService.saveBatch(chapters);
    }
}
