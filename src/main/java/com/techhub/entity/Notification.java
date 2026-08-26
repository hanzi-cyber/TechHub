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
 * 通知表
 */
@Data
@TableName("t_notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收者ID */
    private Long userId;

    /** 触发者ID */
    private Long senderId;

    /** 1点赞 2评论 3关注 4系统通知 */
    private Integer type;

    /** 目标类型:1帖子 2评论 */
    private Integer targetType;

    /** 目标ID(点击跳转用) */
    private Long targetId;

    /** 通知内容(冗余快照,避免回查) */
    private String content;

    /** 0未读 1已读 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
