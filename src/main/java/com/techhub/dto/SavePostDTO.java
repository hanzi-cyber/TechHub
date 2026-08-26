package com.techhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发布/编辑帖子请求参数
 */
@Data
public class SavePostDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String content;

    /** 摘要:列表页展示 */
    private String summary;

    /** 标签ID列表 */
    private List<Long> tagIds;
}
