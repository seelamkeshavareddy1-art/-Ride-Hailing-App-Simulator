
package com.example.ridego.service;

import com.example.ridego.model.Driver;
import com.example.ridego.model.Location;
import com.example.ridego.model.Ride;
import com.example.ridego.model.Rider;
import com.example.ridego.model.Vehicle;
import com.example.ridego.model.Auto;
import com.example.ridego.model.Mini;
import com.example.ridego.model.Sedan;

import com.example.ridego.fare.FareCalculator;
import com.example.ridego.fare.NormalFare;
import com.example.ridego.fare.SurgeFare;

import com.example.ridego.exception.NoDriverAvailableException;

import java.util.List;
import java.util.UUID;

public class RideRequestService {

    private final DriverMatchingService matchingService;

    public RideRequestService() {
        matchingService = new DriverMatchingService();
    }

    // Normal fare by default.
    public Ride requestRide(
            Rider rider,
            Location pickupLocation,
            Location dropLocation,
            List<Driver> availableDrivers
    ) throws NoDriverAvailableException {

        return requestRide(
                rider, pickupLocation, dropLocation,
                availableDrivers, false
        );
    }

    // Supports normal and surge fare.
    public Ride requestRide(
            Rider rider,
            Location pickupLocation,
            Location dropLocation,
            List<Driver> availableDrivers,
            boolean surgeEnabled
    ) throws NoDriverAvailableException {

        if (rider == null || pickupLocation == null
                || dropLocation == null || availableDrivers == null) {
            throw new IllegalArgumentException(
                    "Rider, locations and drivers cannot be null."
            );
        }

        double distance =
                pickupLocation.calculateDistance(dropLocation);

        if (distance <= 0) {
            throw new IllegalArgumentException(
                    "Pickup and drop locations must be different."
            );
        }

        // Synchronized matching reserves the driver.
        Driver assignedDriver =
                matchingService.findNearestAvailableDriver(
                        availableDrivers, pickupLocation
                );

        try {
            Vehicle vehicle = createVehicle(assignedDriver);

            FareCalculator calculator = surgeEnabled
                    ? new SurgeFare(1.5)
                    : new NormalFare();

            double fare =
                    calculator.calculateFare(vehicle, distance);

            Ride ride = new Ride(
                    UUID.randomUUID().toString(),
                    rider,
                    pickupLocation,
                    dropLocation,
                    distance
            );

            ride.setDriver(assignedDriver);
            ride.setFare(fare);
            ride.setStatus("Driver Assigned");

            return ride;

        } catch (RuntimeException e) {
            // Release the reserved driver safely.
            matchingService.releaseDriver(
                    availableDrivers, assignedDriver
            );
            throw e;
        }
    }

    private Vehicle createVehicle(Driver driver) {

        String type = driver.getVehicleType();
        String number = driver.getVehicleNumber();

        if ("Auto".equalsIgnoreCase(type)) {
            return new Auto(number);
        }

        if ("Mini".equalsIgnoreCase(type)) {
            return new Mini(number);
        }

        if ("Sedan".equalsIgnoreCase(type)) {
            return new Sedan(number);
        }

        throw new IllegalArgumentException(
                "Unknown vehicle type: " + type
        );
    }

    // Complete ride and release its driver.
    public void completeRide(
            Ride ride,
            List<Driver> availableDrivers
    ) {
        if (ride == null || ride.getDriver() == null) {
            return;
        }

        synchronized (availableDrivers) {
            if (!"Driver Assigned".equals(ride.getStatus())) {
                return;
            }

            ride.setStatus("Completed");

            matchingService.releaseDriver(
                    availableDrivers, ride.getDriver()
            );
        }
    }

    // Cancel ride and release its driver.
    public void cancelRide(
            Ride ride,
            List<Driver> availableDrivers
    ) {
        if (ride == null || ride.getDriver() == null) {
            return;
        }

        synchronized (availableDrivers) {
            if (!"Driver Assigned".equals(ride.getStatus())) {
                return;
            }

            ride.setStatus("Cancelled");

            matchingService.releaseDriver(
                    availableDrivers, ride.getDriver()
            );
        }
    }
}
