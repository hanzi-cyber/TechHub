package com.techhub.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 所属帖子ID */
    private Long postId;

    /** 评论者ID */
    private Long userId;

    /** 父评论ID */
    private Long parentId;

    /** 被回复用户ID */
    private Long replyToUserId;

    private String content;

    private Integer likeCount;

    private LocalDateTime createdAt;

    /** 评论者信息 */
    private UserVO user;
}
