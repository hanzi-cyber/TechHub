package com.techhub.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectResultVO {
    private static final long serialVersionUID = 1L;

    /** 操作后是否处于已收藏状态 */
    private Boolean collected;

    /** 操作后该目标的最新收藏数 */
    private Integer collectCount;
}
