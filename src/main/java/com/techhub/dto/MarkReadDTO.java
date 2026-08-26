package com.techhub.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通知标记已读请求参数
 */
@Data
public class MarkReadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知ID列表 */
    @NotEmpty(message = "通知ID列表不能为空")
    private List<Long> ids;
}
