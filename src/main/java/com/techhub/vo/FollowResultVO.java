package com.techhub.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 关注/取消关注 返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 操作后是否处于已关注状态 */
    private Boolean followed;
}
