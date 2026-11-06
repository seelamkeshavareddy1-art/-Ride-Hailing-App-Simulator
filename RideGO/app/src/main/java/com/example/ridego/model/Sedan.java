
package com.example.ridego.model;

public class Sedan extends Vehicle {

    public Sedan(String vehicleNumber) {
        super(vehicleNumber, 80.0, 20.0);
    }

    @Override
    public String getVehicleType() {
        return "Sedan";
    }
}