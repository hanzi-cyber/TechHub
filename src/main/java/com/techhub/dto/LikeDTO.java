package com.techhub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 点赞请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标类型:1帖子 2评论 */
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    /** 目标ID */
    @NotNull(message = "目标ID不能为空")
    private Long targetId;
}
