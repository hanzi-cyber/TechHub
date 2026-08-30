package com.techhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techhub.entity.LikeRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 点赞记录 Mapper
 */
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {

    /**
     * 原子点赞:利用唯一键 uk_user_target,已存在则置 status=1。
     * <p>返回值(受影响行数):1=新插入,2=由取消恢复为点赞,0=本来已赞(幂等,无变化)。
     */
    @Insert("INSERT INTO t_like_record (user_id, target_type, target_id, status, created_at, updated_at) " +
            "VALUES (#{userId}, #{targetType}, #{targetId}, 1, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE status = 1, updated_at = NOW()")
    int insertOrLike(@Param("userId") Long userId,
                     @Param("targetType") Integer targetType,
                     @Param("targetId") Long targetId);

    /**
     * 取消点赞:仅当 status=1 时才置 0,返回受影响行数(0=本来就没赞,幂等)。
     */
    @Update("UPDATE t_like_record SET status = 0, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} AND status = 1")
    int unlike(@Param("userId") Long userId,
               @Param("targetType") Integer targetType,
               @Param("targetId") Long targetId);
}
