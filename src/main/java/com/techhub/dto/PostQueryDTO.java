package com.techhub.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子分页列表查询参数
 */
@Data
public class PostQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码,从1开始 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 排序方式:latest最新 hot热门 */
    private String sort = "latest";

    /** 关键词搜索(标题/摘要) */
    private String keyword;

    /** 标签ID过滤 */
    private Long tagId;
}
