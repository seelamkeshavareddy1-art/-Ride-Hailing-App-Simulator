
package com.example.ridego.fare;

import com.example.ridego.model.Vehicle;

public interface FareCalculator {

    double calculateFare(Vehicle vehicle, double distance);
}
