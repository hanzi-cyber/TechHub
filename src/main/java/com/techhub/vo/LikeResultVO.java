package com.techhub.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 点赞/取消点赞 返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 操作后是否处于已点赞状态 */
    private Boolean liked;

    /** 操作后该目标的最新点赞数 */
    private Integer likeCount;
}
