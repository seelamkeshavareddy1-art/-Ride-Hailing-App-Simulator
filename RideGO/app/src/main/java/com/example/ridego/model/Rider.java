
package com.example.ridego.model;

public class Rider extends User {

    public Rider(String name, String email,
                 String phone, String passwordHash) {

        super(name, email, phone, passwordHash);
    }

    @Override
    public String getRole() {
        return "Rider";
    }

    public void requestRide() {
        System.out.println("Rider is requesting a ride.");
    }
}
