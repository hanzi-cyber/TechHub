package com.techhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发表评论请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    /** 父评论ID(回复评论时传) */
    private Long parentId;

    /** 被回复用户ID */
    private Long replyToUserId;
}
