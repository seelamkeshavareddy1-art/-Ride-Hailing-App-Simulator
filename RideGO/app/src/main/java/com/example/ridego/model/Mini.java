
package com.example.ridego.model;

public class Mini extends Vehicle {

    public Mini(String vehicleNumber) {
        super(vehicleNumber, 50.0, 15.0);
    }

    @Override
    public String getVehicleType() {
        return "Mini";
    }
}
