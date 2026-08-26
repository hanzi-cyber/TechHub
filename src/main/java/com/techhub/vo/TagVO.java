package com.techhub.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 标签返回结果
 */
@Data
public class TagVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String description;

    /** 该标签下的帖子数 */
    private Integer postCount;
}
