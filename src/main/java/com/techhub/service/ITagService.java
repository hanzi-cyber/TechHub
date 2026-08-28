package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.entity.Tag;
import com.techhub.vo.TagVO;

import java.util.List;

public interface ITagService extends IService<Tag> {
    List<TagVO> getTags();
}
