package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.PageResult;
import com.techhub.entity.Notification;
import com.techhub.mapper.NotificationMapper;
import com.techhub.service.INotificationService;
import com.techhub.vo.NotificationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    private final Integer IS_READ = 1;
    private final Integer IS_NOT_READ = 0;
    @Override
    public PageResult<NotificationVO> getNotifications(Integer pageNum, Integer pageSize) {
        return null;
    }
}
