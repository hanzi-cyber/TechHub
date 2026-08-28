package com.techhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.entity.Tag;
import com.techhub.mapper.TagMapper;
import com.techhub.service.ITagService;
import com.techhub.vo.TagVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {
    @Override
    public List<TagVO> getTags() {
        List<Tag> tags = list();
        List<TagVO> tagVOList = tags.stream().map(tag -> {
            TagVO tagVO = new TagVO();
            BeanUtils.copyProperties(tag, tagVO);
            return tagVO;
        }).toList();
        return tagVOList;
    }
}
