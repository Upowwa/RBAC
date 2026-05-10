package com.example.userservice;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/passengers")
    public Passenger createPassenger(@Valid @RequestBody Passenger passenger) {
        return userService.createPassenger(passenger);
    }

    @GetMapping("/passengers/{id}")
    public Passenger getPassenger(@PathVariable Long id) {
        return userService.getPassengerById(id);
    }

    @PostMapping("/drivers")
    public Driver createDriver(@Valid @RequestBody Driver driver) {
        return userService.createDriver(driver);
    }

    @GetMapping("/drivers/{id}")
    public Driver getDriver(@PathVariable Long id) {
        return userService.getDriverById(id);
    }

    @PatchMapping("/drivers/{id}/status")
    public Driver updateDriverStatus(@PathVariable Long id, @Valid @RequestBody DriverStatusRequest request) {
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("Field 'status' is required");
        }
        return userService.updateDriverStatus(id, request.getStatus());
    }

    @PostMapping("/drivers/assign")
    public Driver assignAvailableDriver() {
        return userService.assignAvailableDriver();
    }

    public static class DriverStatusRequest {
        @NotNull
        private DriverStatus status;

        public DriverStatus getStatus() {
            return status;
        }

        public void setStatus(DriverStatus status) {
            this.status = status;
        }
    }
}
