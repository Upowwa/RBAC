package com.example.tripservice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trips")
class TripController {

    private final TripService tripService;

    TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Trip createTrip(@RequestBody Map<String, Object> body) {
        Long passengerId = body.get("passenger_id") == null ? null : Long.valueOf(body.get("passenger_id").toString());
        String origin = body.get("origin") == null ? null : body.get("origin").toString();
        String destination = body.get("destination") == null ? null : body.get("destination").toString();
        return tripService.createTrip(passengerId, origin, destination);
    }

    @GetMapping("/{id}")
    public Trip getTrip(@PathVariable Long id) {
        return tripService.getTrip(id);
    }

    @GetMapping
    public List<Trip> getTripsByPassenger(@RequestParam("passenger_id") Long passengerId) {
        return tripService.getTripsByPassenger(passengerId);
    }

    @PatchMapping("/{id}/status")
    public Trip updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Field 'status' is required");
        }
        return tripService.updateStatus(id, status);
    }
}

@RestControllerAdvice
class TripExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleIllegalState(IllegalStateException e) {
        return Map.of("error", e.getMessage());
    }
}

class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}