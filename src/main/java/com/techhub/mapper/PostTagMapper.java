package com.techhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techhub.entity.PostTag;

/**
 * 帖子-标签关联 Mapper
 * 注意:复合主键,查询/插入用 Wrapper 或自定义 XML,不要用 selectById
 */
public interface PostTagMapper extends BaseMapper<PostTag> {
}
