package com.example.ridego.service;

import com.example.ridego.model.Driver;
import com.example.ridego.model.Location;
import com.example.ridego.exception.NoDriverAvailableException;

import java.util.List;

public class DriverMatchingService {

    public Driver findNearestAvailableDriver(
            List<Driver> drivers,
            Location pickupLocation
    ) throws NoDriverAvailableException {

        if (drivers == null || pickupLocation == null) {
            throw new IllegalArgumentException(
                    "Drivers list and pickup location cannot be null."
            );
        }

        // Synchronize the complete driver selection and reservation.
        synchronized (drivers) {

            Driver nearestDriver = null;
            double minimumDistance = Double.MAX_VALUE;

            for (Driver driver : drivers) {

                if (driver == null || !driver.isAvailable()) {
                    continue;
                }

                Location driverLocation = new Location(
                        "Driver Location",
                        driver.getCurrentLatitude(),
                        driver.getCurrentLongitude()
                );

                double distance =
                        pickupLocation.calculateDistance(driverLocation);

                if (distance < minimumDistance) {
                    minimumDistance = distance;
                    nearestDriver = driver;
                }
            }

            if (nearestDriver == null) {
                throw new NoDriverAvailableException();
            }

            // Reserve the driver before releasing the lock.
            nearestDriver.setAvailable(false);

            return nearestDriver;
        }
    }

    public void releaseDriver(
            List<Driver> drivers,
            Driver driver
    ) {
        if (drivers == null || driver == null) {
            return;
        }

        synchronized (drivers) {
            driver.setAvailable(true);
        }
    }
}
