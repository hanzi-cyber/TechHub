package com.techhub.mq;

import com.techhub.common.MqConstants;
import com.techhub.common.NotificationConstants;
import com.techhub.entity.Comment;
import com.techhub.entity.Post;
import com.techhub.mapper.CommentMapper;
import com.techhub.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 通知生产者:把互动事件(点赞/评论/收藏/关注)封装成消息投递到 RabbitMQ
 * 统一处理:查接收者、过滤自己通知自己、拼 content 快照、事务提交后再发送
 */
@Slf4j
@Component
public class NotificationProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;

    /** 点赞:targetType 1=帖子 2=评论(与 LikeDTO 语义一致) */
    public void notifyLike(Integer targetType, Long targetId, Long senderId) {
        if (targetId == null || senderId == null) {
            return;
        }
        Long receiverId;
        Long postId;
        String content;
        if (Integer.valueOf(NotificationConstants.LIKE_TARGET_POST).equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            if (post == null) {
                return;
            }
            receiverId = post.getUserId();
            postId = post.getId();
            content = "赞了你的帖子《" + post.getTitle() + "》";
        } else {
            // 赞评论:接收者是评论作者,点击跳转到评论所属帖子
            Comment comment = commentMapper.selectById(targetId);
            if (comment == null) {
                return;
            }
            receiverId = comment.getUserId();
            postId = comment.getPostId();
            content = "赞了你的评论";
        }
        send(receiverId, senderId, NotificationConstants.TYPE_LIKE, NotificationConstants.TARGET_POST, postId, content);
    }

    /** 评论:一级评论通知帖子作者,楼中楼回复通知被回复者 */
    public void notifyComment(Long postId, Long senderId, String commentContent, Long replyToUserId) {
        if (postId == null || senderId == null) {
            return;
        }
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return;
        }
        String preview = truncate(commentContent);
        if (replyToUserId != null) {
            send(replyToUserId, senderId, NotificationConstants.TYPE_COMMENT, NotificationConstants.TARGET_POST, postId, "回复了你: " + preview);
        } else {
            send(post.getUserId(), senderId, NotificationConstants.TYPE_COMMENT, NotificationConstants.TARGET_POST, postId, "评论了你的帖子《" + post.getTitle() + "》: " + preview);
        }
    }

    /** 收藏:通知帖子作者 */
    public void notifyCollect(Long postId, Long senderId) {
        if (postId == null || senderId == null) {
            return;
        }
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return;
        }
        send(post.getUserId(), senderId, NotificationConstants.TYPE_COLLECT, NotificationConstants.TARGET_POST, postId, "收藏了你的帖子《" + post.getTitle() + "》");
    }

    /** 关注:通知被关注者 */
    public void notifyFollow(Long followeeId, Long senderId) {
        if (followeeId == null || senderId == null) {
            return;
        }
        send(followeeId, senderId, NotificationConstants.TYPE_FOLLOW, NotificationConstants.TARGET_USER, senderId, "关注了你");
    }

    /** 组装消息并在事务提交后投递 */
    private void send(Long receiverId, Long senderId, int type, int targetType, Long targetId, String content) {
        // 自己给自己的互动不通知
        if (receiverId == null || receiverId.equals(senderId)) {
            return;
        }
        NotificationMessage message = NotificationMessage.builder()
                .userId(receiverId)
                .senderId(senderId)
                .type(type)
                .targetType(targetType)
                .targetId(targetId)
                .content(content)
                .build();
        // 事务提交后再发:避免事务回滚了消息却已发出,产生"幽灵通知"
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(message);
                }
            });
        } else {
            doSend(message);
        }
    }

    private void doSend(NotificationMessage message) {
        try {
            rabbitTemplate.convertAndSend(MqConstants.NOTIFICATION_EXCHANGE, MqConstants.NOTIFICATION_ROUTING_KEY, message);
        } catch (Exception e) {
            // 发送失败不能影响主流程,记录日志即可
            log.error("通知消息发送失败: {}", message, e);
        }
    }

    private String truncate(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        return content.length() <= NotificationConstants.CONTENT_PREVIEW_LENGTH
                ? content
                : content.substring(0, NotificationConstants.CONTENT_PREVIEW_LENGTH) + "...";
    }
}
