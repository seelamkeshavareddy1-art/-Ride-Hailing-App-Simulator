
package com.example.ridego.model;

public abstract class Vehicle {

    private String vehicleNumber;
    private double baseFare;
    private double farePerKm;

    public Vehicle(String vehicleNumber,
                   double baseFare,
                   double farePerKm) {

        this.vehicleNumber = vehicleNumber;
        this.baseFare = baseFare;
        this.farePerKm = farePerKm;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getFarePerKm() {
        return farePerKm;
    }

    public abstract String getVehicleType();

    public double calculateFare(double distance) {
        return baseFare + (distance * farePerKm);
    }
}
