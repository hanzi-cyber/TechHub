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
 * 帖子表
 */
@Data
@TableName("t_post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者ID */
    private Long userId;

    private String title;

    /** 摘要:列表页展示,避免查 content 大字段 */
    private String summary;

    /** 正文(Markdown) */
    private String content;

    /** 1已发布 0草稿 2已删除 3待审核 */
    private Integer status;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    /** 热度分,定时任务计算,用于排行 */
    private Long hotScore;

    /** 是否置顶:1是 0否 */
    private Integer isTop;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 发布时间(草稿转发布时写入) */
    private LocalDateTime publishedAt;
}
