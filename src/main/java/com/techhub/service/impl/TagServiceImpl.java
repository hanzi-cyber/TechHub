package com.techhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.entity.Tag;
import com.techhub.mapper.TagMapper;
import com.techhub.service.ITagService;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {
}
