package com.techhub.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 作者ID */
    private Long userId;

    private String title;

    /** 摘要:列表页展示 */
    private String summary;

    /** 正文(Markdown) */
    private String content;

    /** 1已发布 0草稿 2已删除 3待审核 */
    private Integer status;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    /** 是否置顶:1是 0否 */
    private Integer isTop;

    private LocalDateTime createdAt;

    /** 发布时间 */
    private LocalDateTime publishedAt;

    /** 作者信息 */
    private UserVO author;

    /** 标签列表 */
    private List<TagVO> tags;
}
