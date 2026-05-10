package com.example.notificationservice;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationTask createNotification(@Valid @RequestBody NotificationTask task) {
        return notificationService.createTask(task);
    }

    @GetMapping
    public List<NotificationTask> getNotificationsByTripId(@RequestParam("trip_id") Long tripId) {
        return notificationService.getByTripId(tripId);
    }
}