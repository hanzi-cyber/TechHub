package com.techhub.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论表(多级评论:parentId=0 为一级评论,否则为楼中楼回复)
 */
@Data
@TableName("t_comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属帖子ID */
    private Long postId;

    /** 评论者ID */
    private Long userId;

    /** 父评论ID,0 表示一级评论 */
    private Long parentId;

    /** 回复的目标用户ID(楼中楼) */
    private Long replyToUserId;

    private String content;

    private Integer likeCount;

    /** 1正常 0已删除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
