package com.techhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.entity.Notification;
import com.techhub.mapper.NotificationMapper;
import com.techhub.service.INotisficationService;
import org.springframework.stereotype.Service;

@Service
public class NotisficationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotisficationService {
}
