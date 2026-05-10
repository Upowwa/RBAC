package com.example.userservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    public UserService(PassengerRepository passengerRepository, DriverRepository driverRepository) {
        this.passengerRepository = passengerRepository;
        this.driverRepository = driverRepository;
    }

    public Passenger createPassenger(Passenger passenger) {
        return passengerRepository.save(passenger);
    }

    public Passenger getPassengerById(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Passenger not found with id=" + id));
    }

    public Driver createDriver(Driver driver) {
        return driverRepository.save(driver);
    }

    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Driver not found with id=" + id));
    }

    public Driver updateDriverStatus(Long id, DriverStatus status) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Driver not found with id=" + id));

        driver.setStatus(status);
        return driverRepository.save(driver);
    }

    @Transactional
    public Driver assignAvailableDriver() {
        List<Driver> drivers = driverRepository.findAvailableDriversForUpdate();

        if (drivers.isEmpty()) {
            throw new IllegalStateException("No available drivers");
        }

        Driver driver = drivers.get(0);
        driver.setStatus(DriverStatus.BUSY);
        return driverRepository.save(driver);
    }
}