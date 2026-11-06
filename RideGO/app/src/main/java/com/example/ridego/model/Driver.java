
package com.example.ridego.model;

public class Driver extends User {

    private String license;
    private String vehicleNumber;
    private String vehicleType;

    private boolean available;

    private double currentLatitude;
    private double currentLongitude;

    public Driver(String name, String email,
                  String phone, String passwordHash,
                  String license, String vehicleNumber,
                  String vehicleType) {

        super(name, email, phone, passwordHash);

        this.license = license;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;

        this.available = true;

        // Default location until driver's location is updated
        this.currentLatitude = 17.3850;
        this.currentLongitude = 78.4867;
    }

    @Override
    public String getRole() {
        return "Driver";
    }

    public String getLicense() {
        return license;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }
}
