package com.techhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techhub.entity.PostTag;
import com.techhub.vo.TagVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子-标签关联 Mapper
 * 注意:复合主键,查询/插入用 Wrapper 或自定义 SQL,不要用 selectById
 */
@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {

    /**
     * 根据帖子id获取标签列表
     * @param postId 帖子id
     * @return 标签列表
     */
    @Select("SELECT t.id, t.name, t.description, t.post_count " +
            "FROM t_tag t INNER JOIN t_post_tag pt ON t.id = pt.tag_id " +
            "WHERE pt.post_id = #{postId}")
    List<TagVO> getTagsByPostId(@Param("postId") Long postId);
}
