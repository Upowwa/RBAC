package com.example.notificationservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public NotificationTask createNotification(@RequestBody NotificationTask task) {
        return notificationService.createTask(task);
    }

    @GetMapping
    public List<NotificationTask> getNotificationsByTripId(@RequestParam("trip_id") Long tripId) {
        return notificationService.getByTripId(tripId);
    }
}