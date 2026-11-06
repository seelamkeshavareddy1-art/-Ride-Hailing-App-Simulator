
package com.example.ridego.service;

import android.content.Context;

import com.example.ridego.model.Ride;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RideHistoryService {

    private static final String FILE_NAME = "ride_history.json";

    private final Context context;

    public RideHistoryService(Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized void saveRide(Ride ride) throws Exception {

        if (ride == null) {
            throw new IllegalArgumentException("Ride cannot be null.");
        }

        JSONArray history = readAllRides();
        JSONObject record = new JSONObject();

        record.put("rideId", ride.getRideId());
        record.put("riderName", ride.getRider().getName());
        record.put("riderEmail", ride.getRider().getEmail());

        if (ride.getDriver() != null) {
            record.put("driverName", ride.getDriver().getName());
            record.put("driverEmail", ride.getDriver().getEmail());
            record.put("vehicleType", ride.getDriver().getVehicleType());
            record.put("vehicleNumber", ride.getDriver().getVehicleNumber());
        }

        record.put("pickup", ride.getPickupLocation().getAddress());
        record.put("drop", ride.getDropLocation().getAddress());
        record.put("distance", ride.getDistance());
        record.put("fare", ride.getFare());
        record.put("status", ride.getStatus());
        record.put("rating", 0);
        record.put("timestamp", System.currentTimeMillis());

        boolean updated = false;

        for (int i = 0; i < history.length(); i++) {
            JSONObject oldRecord = history.getJSONObject(i);

            if (ride.getRideId().equals(oldRecord.optString("rideId"))) {
                // Preserve an existing rating when updating a ride.
                record.put("rating", oldRecord.optInt("rating", 0));
                history.put(i, record);
                updated = true;
                break;
            }
        }

        if (!updated) {
            history.put(record);
        }

        writeAllRides(history);
    }

    public synchronized JSONArray readAllRides() throws Exception {

        try (FileInputStream input = context.openFileInput(FILE_NAME);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8))) {

            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            return new JSONArray(content.toString());

        } catch (java.io.FileNotFoundException e) {
            return new JSONArray();
        }
    }

    public synchronized JSONArray getRiderHistory(
            String riderEmail
    ) throws Exception {

        JSONArray allRides = readAllRides();
        JSONArray riderRides = new JSONArray();

        if (riderEmail == null) {
            return riderRides;
        }

        for (int i = 0; i < allRides.length(); i++) {
            JSONObject ride = allRides.getJSONObject(i);

            if (riderEmail.equalsIgnoreCase(
                    ride.optString("riderEmail", ""))) {
                riderRides.put(ride);
            }
        }

        return riderRides;
    }

    private synchronized void writeAllRides(
            JSONArray history
    ) throws Exception {

        try (FileOutputStream output = context.openFileOutput(
                FILE_NAME, Context.MODE_PRIVATE)) {

            output.write(
                    history.toString().getBytes(StandardCharsets.UTF_8)
            );
            output.flush();
        }
    }

    // Calculate total earnings for a driver.
    public synchronized double getDriverEarnings(
            String driverEmail
    ) throws Exception {

        if (driverEmail == null) {
            return 0.0;
        }

        JSONArray rides = readAllRides();
        double totalEarnings = 0.0;

        for (int i = 0; i < rides.length(); i++) {

            JSONObject ride = rides.getJSONObject(i);

            String savedDriverEmail =
                    ride.optString("driverEmail", "");

            String status = ride.optString("status", "");

            if (driverEmail.equalsIgnoreCase(savedDriverEmail)
                    && "Completed".equalsIgnoreCase(status)) {

                totalEarnings += ride.optDouble("fare", 0.0);
            }
        }

        return totalEarnings;
    }

    // Count completed rides for a driver.
    public synchronized int getDriverCompletedRideCount(
            String driverEmail
    ) throws Exception {

        if (driverEmail == null) {
            return 0;
        }

        JSONArray rides = readAllRides();
        int count = 0;

        for (int i = 0; i < rides.length(); i++) {

            JSONObject ride = rides.getJSONObject(i);

            if (driverEmail.equalsIgnoreCase(
                    ride.optString("driverEmail", ""))
                    && "Completed".equalsIgnoreCase(
                    ride.optString("status", ""))) {

                count++;
            }
        }

        return count;
    }

    // Save a rider's rating for a completed ride.
    public synchronized void saveRideRating(
            String rideId,
            String riderEmail,
            int rating
    ) throws Exception {

        if (rideId == null || riderEmail == null) {
            throw new IllegalArgumentException(
                    "Ride ID and rider email are required.");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5.");
        }

        JSONArray rides = readAllRides();
        boolean found = false;

        for (int i = 0; i < rides.length(); i++) {

            JSONObject ride = rides.getJSONObject(i);

            if (rideId.equals(ride.optString("rideId"))
                    && riderEmail.equalsIgnoreCase(
                    ride.optString("riderEmail"))) {

                if (!"Completed".equalsIgnoreCase(
                        ride.optString("status"))) {
                    throw new IllegalStateException(
                            "Only completed rides can be rated.");
                }

                ride.put("rating", rating);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException(
                    "Ride record not found.");
        }

        writeAllRides(rides);
    }


}
