
package com.example.ridego;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ridego.model.Rider;
import com.example.ridego.model.Location;
import com.example.ridego.model.Driver;
import com.example.ridego.model.Ride;

import com.example.ridego.service.AuthService;
import com.example.ridego.service.ConcurrentRideRequestService;
import com.example.ridego.service.RideRequestService;
import com.example.ridego.service.RideHistoryService;

import java.util.List;
import java.util.concurrent.Future;

public class RiderHomeActivity extends AppCompatActivity {

    TextView tvWelcomeRider;

    EditText etPickup, etPickupLatitude, etPickupLongitude;
    EditText etDrop, etDropLatitude, etDropLongitude;

    Button btnBookRide, btnCancelRide, btnCompleteRide;
    Button btnRideHistory;

    Switch switchSurgeFare;

    AuthService authService;
    RideRequestService rideRequestService;
    ConcurrentRideRequestService concurrentService;
    RideHistoryService rideHistoryService;

    Rider rider;
    Ride currentRide;

    private static List<Driver> sharedDrivers;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_home);

        tvWelcomeRider = findViewById(R.id.tvWelcomeRider);

        etPickup = findViewById(R.id.etPickup);
        etPickupLatitude = findViewById(R.id.etPickupLatitude);
        etPickupLongitude = findViewById(R.id.etPickupLongitude);

        etDrop = findViewById(R.id.etDrop);
        etDropLatitude = findViewById(R.id.etDropLatitude);
        etDropLongitude = findViewById(R.id.etDropLongitude);

        btnBookRide = findViewById(R.id.btnBookRide);
        btnCancelRide = findViewById(R.id.btnCancelRide);
        btnCompleteRide = findViewById(R.id.btnCompleteRide);
        btnRideHistory = findViewById(R.id.btnRideHistory);

        switchSurgeFare = findViewById(R.id.switchSurgeFare);

        btnCancelRide.setEnabled(false);
        btnCompleteRide.setEnabled(false);

        authService = new AuthService(this);
        rideRequestService = new RideRequestService();
        concurrentService = ConcurrentRideRequestService.getInstance();
        rideHistoryService = new RideHistoryService(this);

        synchronized (RiderHomeActivity.class) {
            if (sharedDrivers == null) {
                try {
                    sharedDrivers = authService.getAllDrivers();
                } catch (Exception e) {
                    Toast.makeText(this,
                            "Unable to load drivers: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    sharedDrivers = new java.util.ArrayList<>();
                }
            }
        }

        String name = getIntent().getStringExtra("userName");
        String email = getIntent().getStringExtra("userEmail");
        String phone = getIntent().getStringExtra("userPhone");
        String passwordHash =
                getIntent().getStringExtra("passwordHash");

        rider = new Rider(name, email, phone, passwordHash);

        tvWelcomeRider.setText("Welcome, " + name + "!");

        btnBookRide.setOnClickListener(v -> bookRide());
        btnCancelRide.setOnClickListener(v -> cancelCurrentRide());
        btnCompleteRide.setOnClickListener(v -> completeCurrentRide());

        // Open Ride History screen.
        btnRideHistory.setOnClickListener(v -> {

            if (email == null || email.trim().isEmpty()) {
                Toast.makeText(this,
                        "Rider email not found. Please login again.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(
                    RiderHomeActivity.this,
                    RideHistoryActivity.class
            );

            intent.putExtra("userEmail", email);
            startActivity(intent);
        });
    }

    private void bookRide() {

        if (currentRide != null
                && "Driver Assigned".equals(currentRide.getStatus())) {

            Toast.makeText(this,
                    "Please complete or cancel your current ride first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String pickupAddress =
                    etPickup.getText().toString().trim();

            String dropAddress =
                    etDrop.getText().toString().trim();

            if (pickupAddress.isEmpty() || dropAddress.isEmpty()
                    || etPickupLatitude.getText().toString().trim().isEmpty()
                    || etPickupLongitude.getText().toString().trim().isEmpty()
                    || etDropLatitude.getText().toString().trim().isEmpty()
                    || etDropLongitude.getText().toString().trim().isEmpty()) {

                Toast.makeText(this,
                        "Please enter all pickup and drop details",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            double pickupLat = Double.parseDouble(
                    etPickupLatitude.getText().toString().trim());

            double pickupLon = Double.parseDouble(
                    etPickupLongitude.getText().toString().trim());

            double dropLat = Double.parseDouble(
                    etDropLatitude.getText().toString().trim());

            double dropLon = Double.parseDouble(
                    etDropLongitude.getText().toString().trim());

            if (pickupLat < -90 || pickupLat > 90
                    || dropLat < -90 || dropLat > 90
                    || pickupLon < -180 || pickupLon > 180
                    || dropLon < -180 || dropLon > 180) {

                Toast.makeText(this,
                        "Please enter valid coordinates",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Location pickup = new Location(
                    pickupAddress, pickupLat, pickupLon);

            Location drop = new Location(
                    dropAddress, dropLat, dropLon);

            boolean surgeEnabled = switchSurgeFare.isChecked();

            btnBookRide.setEnabled(false);

            Toast.makeText(this,
                    "Finding your nearest driver...",
                    Toast.LENGTH_SHORT).show();

            Future<Ride> rideFuture =
                    concurrentService.submitRideRequest(
                            rider,
                            pickup,
                            drop,
                            sharedDrivers,
                            surgeEnabled
                    );

            new Thread(() -> {
                try {
                    Ride ride = rideFuture.get();

                    mainHandler.post(() -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        currentRide = ride;

                        btnBookRide.setEnabled(true);
                        btnCancelRide.setEnabled(true);
                        btnCompleteRide.setEnabled(true);

                        showBookingConfirmation(ride, surgeEnabled);
                    });

                } catch (Exception e) {
                    mainHandler.post(() -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnBookRide.setEnabled(true);

                        String errorMessage =
                                e.getCause() != null
                                        ? e.getCause().getMessage()
                                        : e.getMessage();

                        Toast.makeText(this,
                                "Booking failed: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    });
                }
            }).start();

        } catch (NumberFormatException e) {
            btnBookRide.setEnabled(true);
            Toast.makeText(this,
                    "Enter valid numeric coordinates",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            btnBookRide.setEnabled(true);
            Toast.makeText(this,
                    "Booking failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showBookingConfirmation(
            Ride ride,
            boolean surgeEnabled
    ) {

        // Save the newly booked ride.
        try {
            rideHistoryService.saveRide(ride);
        } catch (Exception e) {
            Toast.makeText(this,
                    "Ride history save failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        String fareType = surgeEnabled
                ? "Surge Fare (1.5x)"
                : "Normal Fare";

        String message = "Ride booked successfully!\n\n"
                + "Driver: " + ride.getDriver().getName()
                + "\nVehicle: " + ride.getDriver().getVehicleType()
                + "\nVehicle Number: "
                + ride.getDriver().getVehicleNumber()
                + "\nDistance: "
                + String.format("%.2f km", ride.getDistance())
                + "\nFare Type: " + fareType
                + "\nEstimated Fare: ₹"
                + String.format("%.2f", ride.getFare())
                + "\n\nRide ID: " + ride.getRideId();

        new android.app.AlertDialog.Builder(this)
                .setTitle("RideGo Booking")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void cancelCurrentRide() {

        if (currentRide == null
                || !"Driver Assigned".equals(currentRide.getStatus())) {

            Toast.makeText(this,
                    "No active ride to cancel",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        rideRequestService.cancelRide(
                currentRide, sharedDrivers
        );

        // Save the updated cancellation status.
        try {
            rideHistoryService.saveRide(currentRide);
        } catch (Exception e) {
            Toast.makeText(this,
                    "History update failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        btnCancelRide.setEnabled(false);
        btnCompleteRide.setEnabled(false);

        Toast.makeText(this,
                "Ride cancelled successfully!",
                Toast.LENGTH_LONG).show();

        currentRide = null;
    }

    private void completeCurrentRide() {

        if (currentRide == null
                || !"Driver Assigned".equals(currentRide.getStatus())) {

            Toast.makeText(this,
                    "No active ride to complete",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        rideRequestService.completeRide(
                currentRide, sharedDrivers
        );

        // Save the updated completion status.
        try {
            rideHistoryService.saveRide(currentRide);
        } catch (Exception e) {
            Toast.makeText(this,
                    "History update failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        btnCancelRide.setEnabled(false);
        btnCompleteRide.setEnabled(false);

        Toast.makeText(this,
                "Ride completed successfully!",
                Toast.LENGTH_LONG).show();

        currentRide = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Keep the shared concurrent service alive.
    }
}
