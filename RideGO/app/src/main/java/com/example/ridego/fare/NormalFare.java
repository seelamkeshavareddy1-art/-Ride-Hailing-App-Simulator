
package com.example.ridego.fare;

import com.example.ridego.model.Vehicle;

public class NormalFare implements FareCalculator {

    @Override
    public double calculateFare(Vehicle vehicle, double distance) {

        if (distance < 0) {
            throw new IllegalArgumentException(
                    "Distance cannot be negative");
        }

        return vehicle.calculateFare(distance);
    }
}
