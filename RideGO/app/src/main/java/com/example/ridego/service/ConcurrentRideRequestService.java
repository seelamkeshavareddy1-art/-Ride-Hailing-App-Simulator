
package com.example.ridego.service;

import android.content.Context;

import com.example.ridego.model.Driver;
import com.example.ridego.model.Location;
import com.example.ridego.model.Ride;
import com.example.ridego.model.Rider;

import com.example.ridego.exception.NoDriverAvailableException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcurrentRideRequestService {

    private static ConcurrentRideRequestService instance;

    private final ExecutorService executorService;
    private final RideRequestService rideRequestService;

    private ConcurrentRideRequestService() {
        executorService = Executors.newFixedThreadPool(3);
        rideRequestService = new RideRequestService();
    }

    public static synchronized ConcurrentRideRequestService getInstance() {
        if (instance == null) {
            instance = new ConcurrentRideRequestService();
        }
        return instance;
    }

    public Future<Ride> submitRideRequest(
            Rider rider,
            Location pickup,
            Location drop,
            List<Driver> sharedDrivers,
            boolean surgeEnabled
    ) {
        return executorService.submit(() ->
                rideRequestService.requestRide(
                        rider,
                        pickup,
                        drop,
                        sharedDrivers,
                        surgeEnabled
                )
        );
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
