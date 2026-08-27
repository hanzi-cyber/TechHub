package com.techhub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 收藏请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 帖子ID */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
}
