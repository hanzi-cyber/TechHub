package com.techhub.controller;

import com.techhub.common.PageResult;
import com.techhub.common.Result;
import com.techhub.dto.MarkReadDTO;
import com.techhub.service.INotificationService;
import com.techhub.vo.NotificationVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private INotificationService notificationService;

    @GetMapping
    public Result<PageResult<NotificationVO>> getNotifications(@RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NotificationVO> result = notificationService.getNotifications(pageNum, pageSize);
        return Result.success(result);
    }

    /** 未读通知数(导航栏红点) */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }

    /** 标记已读(仅当前用户自己的通知) */
    @PutMapping("/read")
    public Result<Void> markRead(@Valid @RequestBody MarkReadDTO dto) {
        notificationService.markRead(dto.getIds());
        return Result.success();
    }
}
