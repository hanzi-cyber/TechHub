package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.common.PageResult;
import com.techhub.entity.Notification;
import com.techhub.vo.NotificationVO;

public interface INotificationService extends IService<Notification> {
    PageResult<NotificationVO> getNotifications(Integer pageNum, Integer pageSize);
}
