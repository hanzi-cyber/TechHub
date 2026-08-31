package com.techhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techhub.entity.Follow;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 关注关系 Mapper
 */
public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 原子关注:利用唯一键 uk_user_followee,已存在则置 status=1。
     * 返回受影响行数:1=新插入,2=由取关恢复为关注,0=本来已关注(幂等)。
     */
    @Insert("INSERT INTO t_follow (user_id, followee_id, status, created_at) VALUES " +
            "(#{userId}, #{followeeId}, 1, NOW()) " +
            "ON DUPLICATE KEY UPDATE status = 1")
    int follow(@Param("userId") Long userId, @Param("followeeId") Long followeeId);

    /**
     * 取消关注:仅当 status=1 时才置 0,返回受影响行数(0=本来就没关注,幂等)。
     */
    @Update("UPDATE t_follow SET status = 0 " +
            "WHERE user_id = #{userId} AND followee_id = #{followeeId} AND status = 1")
    int unfollow(@Param("userId") Long userId, @Param("followeeId") Long followeeId);
}
