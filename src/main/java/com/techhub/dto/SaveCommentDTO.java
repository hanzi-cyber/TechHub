package com.techhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发布评论 / 楼中楼回复请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveCommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 父评论ID:0 或不传 = 一级评论;传楼层id = 在该楼层下回复 */
    private Long parentId;

    /** 被回复用户ID(楼中楼 @ 某人用,可不传,默认回复父评论作者) */
    private Long replyToUserId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
