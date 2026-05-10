package com.example.tripservice;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
}