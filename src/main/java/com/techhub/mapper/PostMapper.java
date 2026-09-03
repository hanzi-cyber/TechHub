package com.techhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.techhub.entity.Post;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 帖子 Mapper
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 关注流(拉模式):一条 SQL 用子查询圈定「我关注的人」,再按发布时间倒序分页。
     * 拉模式特点:发帖不做任何扇出,读 feed 时实时查询,天然一致、无脏数据。
     *
     * @param page   分页参数(第一个参数必须传 IPage,分页插件才会自动拼 LIMIT)
     * @param userId 当前用户ID
     */
    @Select("SELECT * FROM t_post p " +
            "WHERE p.status = 1 AND p.user_id IN (" +
            "  SELECT followee_id FROM t_follow WHERE user_id = #{userId} AND status = 1" +
            ") ORDER BY p.published_at DESC")
    IPage<Post> selectFeedPage(IPage<Post> page, @Param("userId") Long userId);
}
