package com.techhub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 帖子-标签关联表(复合主键)
 *
 * 注意:MyBatis-Plus 对复合主键支持有限,本表没有单一 @TableId,
 * 查询/插入请用 Wrapper 或自定义 XML,不要用 selectById。
 */
@Data
@TableName("t_post_tag")
public class PostTag implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long postId;

    private Long tagId;
}
