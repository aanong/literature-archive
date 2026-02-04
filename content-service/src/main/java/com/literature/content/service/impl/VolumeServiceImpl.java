package com.literature.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.literature.content.entity.Volume;
import com.literature.content.mapper.VolumeMapper;
import com.literature.content.service.VolumeService;
import org.springframework.stereotype.Service;

@Service
public class VolumeServiceImpl extends ServiceImpl<VolumeMapper, Volume> implements VolumeService {
}
