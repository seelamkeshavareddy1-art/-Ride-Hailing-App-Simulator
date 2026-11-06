
package com.example.ridego.model;

public class Auto extends Vehicle {

    public Auto(String vehicleNumber) {
        super(vehicleNumber, 30.0, 12.0);
    }

    @Override
    public String getVehicleType() {
        return "Auto";
    }
}
