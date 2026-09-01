package com.techhub.controller;

import com.techhub.common.PageResult;
import com.techhub.common.Result;
import com.techhub.service.INotificationService;
import com.techhub.vo.NotificationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private INotificationService notificationService;

    @GetMapping
    public Result<PageResult<NotificationVO>> getNotifications(@RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "10") Integer pageSize){

        PageResult<NotificationVO> result = notificationService.getNotifications(pageNum,pageSize);
        return Result.success(result);
    }
}
