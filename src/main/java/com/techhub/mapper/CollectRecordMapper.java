package com.techhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techhub.entity.CollectRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 收藏记录 Mapper
 */
public interface CollectRecordMapper extends BaseMapper<CollectRecord> {

    @Insert("insert into t_collect_record (user_id, post_id, status, created_at, updated_at) VALUES " +
            "(#{userId},#{postId},1,now(),now())" +
            "ON DUPLICATE KEY UPDATE status = 1,updated_at = now()")
    int creatCollectRecord(@Param(value = "userId") Long userId,@Param(value = "postId") Long postId);

    @Update("update t_collect_record set status = 0 ,updated_at = now() " +
            "where user_id = #{userId} and post_id = #{postId} and status = 1")
    int cancleCollect(@Param(value = "userId") Long userId,@Param(value = "postId") Long postId);
}
