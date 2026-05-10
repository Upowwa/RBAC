package com.example.tripservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
class TripService {
    private final TripRepository tripRepository;
    private final RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://localhost:8081";
    private static final String NOTIFICATION_SERVICE_URL = "http://localhost:8083";

    TripService(TripRepository tripRepository, RestTemplate restTemplate) {
        this.tripRepository = tripRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public Trip createTrip(Long passengerId, String origin, String destination) {
        if (passengerId == null || origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("passenger_id, origin, destination are required");
        }

        verifyPassengerExists(passengerId);

        Map<String, Object> driver = assignAvailableDriver();
        if (driver == null || driver.get("id") == null) {
            throw new IllegalStateException("No available drivers");
        }

        Long driverId = ((Number) driver.get("id")).longValue();

        Trip trip = new Trip();
        trip.setPassengerId(passengerId);
        trip.setDriverId(driverId);
        trip.setOrigin(origin);
        trip.setDestination(destination);
        trip.setStatus(TripStatus.DRIVER_ASSIGNED);
        trip.setPrice(BigDecimal.valueOf(100));

        Trip savedTrip = tripRepository.save(trip);

        createNotification(
                savedTrip.getId(),
                "PASSENGER",
                savedTrip.getPassengerId(),
                "Trip created with status DRIVER_ASSIGNED"
        );

        createNotification(
                savedTrip.getId(),
                "DRIVER",
                savedTrip.getDriverId(),
                "You have been assigned to trip " + savedTrip.getId()
        );

        return savedTrip;
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

        validateStatusTransition(trip.getStatus(), newStatus);

        trip.setStatus(newStatus);

        if (newStatus == TripStatus.ACCEPTED || newStatus == TripStatus.IN_PROGRESS) {
            if (trip.getDriverId() != null) {
                updateDriverStatus(trip.getDriverId(), "BUSY");
            }
        }

        if (newStatus == TripStatus.COMPLETED || newStatus == TripStatus.CANCELLED) {
            if (trip.getDriverId() != null) {
                updateDriverStatus(trip.getDriverId(), "AVAILABLE");
            }
        }

        Trip savedTrip = tripRepository.save(trip);

        createNotification(
                savedTrip.getId(),
                "PASSENGER",
                savedTrip.getPassengerId(),
                "Trip status changed to " + savedTrip.getStatus()
        );

        if (savedTrip.getDriverId() != null) {
            createNotification(
                    savedTrip.getId(),
                    "DRIVER",
                    savedTrip.getDriverId(),
                    "Trip " + savedTrip.getId() + " status changed to " + savedTrip.getStatus()
            );
        }

        return savedTrip;
    }

    private void verifyPassengerExists(Long passengerId) {
        try {
            restTemplate.getForObject(USER_SERVICE_URL + "/passengers/" + passengerId, Object.class);
        } catch (Exception e) {
            throw new NotFoundException("Passenger not found: " + passengerId);
        }
    }

    private Map<String, Object> assignAvailableDriver() {
        try {
            return restTemplate.postForObject(
                    USER_SERVICE_URL + "/drivers/assign",
                    null,
                    Map.class
            );
        } catch (Exception e) {
            return null;
        }
    }

    private void updateDriverStatus(Long driverId, String status) {
        Map<String, String> body = Map.of("status", status);
        restTemplate.patchForObject(
                USER_SERVICE_URL + "/drivers/" + driverId + "/status",
                body,
                Object.class
        );
    }

    private void createNotification(Long tripId, String recipientType, Long recipientId, String message) {
        Map<String, Object> body = Map.of(
                "tripId", tripId,
                "recipientType", recipientType,
                "recipientId", recipientId,
                "message", message
        );

        restTemplate.postForObject(
                NOTIFICATION_SERVICE_URL + "/notifications",
                body,
                Object.class
        );
    }

    private void validateStatusTransition(TripStatus current, TripStatus next) {
        boolean valid =
                (current == TripStatus.DRIVER_ASSIGNED && (next == TripStatus.ACCEPTED || next == TripStatus.CANCELLED)) ||
                        (current == TripStatus.ACCEPTED && (next == TripStatus.IN_PROGRESS || next == TripStatus.CANCELLED)) ||
                        (current == TripStatus.IN_PROGRESS && next == TripStatus.COMPLETED);

        if (!valid) {
            throw new IllegalArgumentException("Invalid status transition: " + current + " -> " + next);
        }
    }
}