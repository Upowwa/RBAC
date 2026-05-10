package com.example.notificationservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findByTripIdOrderByCreatedAtDesc(Long tripId);

    @Query(value = """
            SELECT * FROM notification_tasks
            WHERE status = 'PENDING' AND attempts < 3
            ORDER BY created_at
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<NotificationTask> findNextPendingTaskForUpdate();
}