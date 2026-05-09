package com.example.userservice;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class UserController {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    public UserController(PassengerRepository passengerRepository, DriverRepository driverRepository) {
        this.passengerRepository = passengerRepository;
        this.driverRepository = driverRepository;
    }

    @PostMapping("/passengers")
    @ResponseStatus(HttpStatus.CREATED)
    public Passenger createPassenger(@Valid @RequestBody Passenger passenger) {
        passenger.setId(null);
        return passengerRepository.save(passenger);
    }

    @GetMapping("/passengers/{id}")
    public Passenger getPassenger(@PathVariable Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Passenger not found: " + id));
    }

    @PostMapping("/drivers")
    @ResponseStatus(HttpStatus.CREATED)
    public Driver createDriver(@Valid @RequestBody Driver driver) {
        driver.setId(null);
        if (driver.getStatus() == null) {
            driver.setStatus(DriverStatus.OFFLINE);
        }
        return driverRepository.save(driver);
    }

    @GetMapping("/drivers/{id}")
    public Driver getDriver(@PathVariable Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Driver not found: " + id));
    }

    @PatchMapping("/drivers/{id}/status")
    public Driver updateDriverStatus(@PathVariable Long id,
                                     @RequestBody Map<String, String> body) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Driver not found: " + id));

        String statusValue = body.get("status");
        if (statusValue == null || statusValue.isBlank()) {
            throw new IllegalArgumentException("Field 'status' is required");
        }

        try {
            driver.setStatus(DriverStatus.valueOf(statusValue.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Allowed: AVAILABLE, BUSY, OFFLINE");
        }

        return driverRepository.save(driver);
    }
}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }
}

class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}