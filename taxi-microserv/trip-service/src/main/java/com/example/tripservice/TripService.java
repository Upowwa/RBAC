package com.example.tripservice;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
class TripService {

    private final TripRepository tripRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String USER_SERVICE_URL = "http://localhost:8081";

    TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Transactional
    public Trip createTrip(Long passengerId, String origin, String destination) {
        if (passengerId == null || origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("passenger_id, origin, destination are required");
        }

        verifyPassengerExists(passengerId);

        Map<String, Object> driver = findAndLockAvailableDriver();
        if (driver == null) {
            throw new IllegalStateException("No available drivers");
        }

        Long driverId = ((Number) driver.get("id")).longValue();
        updateDriverStatus(driverId, "BUSY");

        Trip trip = new Trip();
        trip.setPassengerId(passengerId);
        trip.setDriverId(driverId);
        trip.setOrigin(origin);
        trip.setDestination(destination);
        trip.setStatus(TripStatus.DRIVER_ASSIGNED);
        trip.setPrice(java.math.BigDecimal.valueOf(100));
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());

        return tripRepository.save(trip);
    }

    public Trip getTrip(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip not found: " + id));
    }

    public List<Trip> getTripsByPassenger(Long passengerId) {
        verifyPassengerExists(passengerId);
        return tripRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);
    }

    @Transactional
    public Trip updateStatus(Long id, String statusValue) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip not found: " + id));

        TripStatus newStatus;
        try {
            newStatus = TripStatus.valueOf(statusValue.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid trip status");
        }

        trip.setStatus(newStatus);
        trip.setUpdatedAt(LocalDateTime.now());

        if (newStatus == TripStatus.ACCEPTED || newStatus == TripStatus.IN_PROGRESS) {
            if (trip.getDriverId() != null) {
                updateDriverStatus(trip.getDriverId(), "BUSY");
            }
        }

        if (newStatus == TripStatus.COMPLETED) {
            if (trip.getDriverId() != null) {
                updateDriverStatus(trip.getDriverId(), "AVAILABLE");
            }
        }

        return tripRepository.save(trip);
    }

    private void verifyPassengerExists(Long passengerId) {
        try {
            restTemplate.getForObject(USER_SERVICE_URL + "/passengers/" + passengerId, Object.class);
        } catch (Exception e) {
            throw new NotFoundException("Passenger not found: " + passengerId);
        }
    }

    private Map<String, Object> findAndLockAvailableDriver() {
        try {
            return restTemplate.getForObject(USER_SERVICE_URL + "/drivers?status=AVAILABLE&lock=true", Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateDriverStatus(Long driverId, String status) {
        Map<String, String> body = Map.of("status", status);
        restTemplate.patchForObject(USER_SERVICE_URL + "/drivers/" + driverId + "/status", body, Object.class);
    }
}