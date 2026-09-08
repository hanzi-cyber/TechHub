package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.PageResult;
import com.techhub.context.BaseContext;
import com.techhub.entity.Notification;
import com.techhub.entity.User;
import com.techhub.mapper.NotificationMapper;
import com.techhub.mapper.UserMapper;
import com.techhub.service.INotificationService;
import com.techhub.vo.NotificationVO;
import com.techhub.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<NotificationVO> getNotifications(Integer pageNum, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        Page<Notification> page = notificationMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreatedAt));

        PageResult<NotificationVO> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());

        List<Notification> records = page.getRecords();
        if (records.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 批量查触发者信息(去重),填充 sender 用户名/头像
        List<Long> senderIds = records.stream()
                .map(Notification::getSenderId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserVO> senderMap = userMapper.selectBatchIds(senderIds).stream()
                .collect(Collectors.toMap(User::getId, user -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                }));

        List<NotificationVO> vos = records.stream().map(n -> {
            NotificationVO vo = new NotificationVO();
            BeanUtils.copyProperties(n, vo);
            vo.setSender(senderMap.get(n.getSenderId()));
            return vo;
        }).collect(Collectors.toList());
        result.setRecords(vos);
        return result;
    }

    @Override
    public long getUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Long userId = BaseContext.getCurrentId();
        // 只更新当前用户自己的通知,防止越权把别人的通知标为已读
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .in(Notification::getId, ids)
                .set(Notification::getIsRead, 1));
    }
}
