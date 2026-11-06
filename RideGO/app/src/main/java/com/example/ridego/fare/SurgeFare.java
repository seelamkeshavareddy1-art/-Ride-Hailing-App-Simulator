
package com.example.ridego.fare;

import com.example.ridego.model.Vehicle;

public class SurgeFare implements FareCalculator {

    private double surgeMultiplier;

    public SurgeFare(double surgeMultiplier) {

        if (surgeMultiplier < 1.0) {
            throw new IllegalArgumentException(
                    "Surge multiplier must be at least 1.0");
        }

        this.surgeMultiplier = surgeMultiplier;
    }

    @Override
    public double calculateFare(Vehicle vehicle, double distance) {

        if (distance < 0) {
            throw new IllegalArgumentException(
                    "Distance cannot be negative");
        }

        double normalFare = vehicle.calculateFare(distance);

        return normalFare * surgeMultiplier;
    }

    public double getSurgeMultiplier() {
        return surgeMultiplier;
    }
}