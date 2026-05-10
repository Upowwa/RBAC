package com.example.notificationservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationTaskRepository repository;

    public NotificationService(NotificationTaskRepository repository) {
        this.repository = repository;
    }

    public NotificationTask createTask(NotificationTask task) {
        task.setStatus(NotificationStatus.PENDING);
        task.setAttempts(0);
        return repository.save(task);
    }

    public List<NotificationTask> getByTripId(Long tripId) {
        return repository.findByTripIdOrderByCreatedAtDesc(tripId);
    }

    @Transactional
    public NotificationTask claimNextTask() {
        return repository.findNextPendingTaskForUpdate()
                .map(task -> {
                    task.setStatus(NotificationStatus.PROCESSING);
                    return repository.save(task);
                })
                .orElse(null);
    }

    @Transactional
    public void markSent(Long taskId) {
        repository.findById(taskId).ifPresent(task -> {
            task.setStatus(NotificationStatus.SENT);
            repository.save(task);
        });
    }

    @Transactional
    public void markFailedOrRetry(Long taskId) {
        repository.findById(taskId).ifPresent(task -> {
            int nextAttempts = task.getAttempts() + 1;
            task.setAttempts(nextAttempts);

            if (nextAttempts >= 3) {
                task.setStatus(NotificationStatus.FAILED);
            } else {
                task.setStatus(NotificationStatus.PENDING);
            }

            repository.save(task);
        });
    }
}