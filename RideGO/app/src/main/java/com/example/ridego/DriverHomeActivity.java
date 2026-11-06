
package com.example.ridego;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ridego.service.RideHistoryService;

import java.util.Locale;

public class DriverHomeActivity extends AppCompatActivity {

    TextView tvWelcomeDriver;
    TextView tvDriverEarnings;

    RideHistoryService rideHistoryService;

    String driverEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        tvWelcomeDriver = findViewById(R.id.tvWelcomeDriver);
        tvDriverEarnings = findViewById(R.id.tvDriverEarnings);

        rideHistoryService = new RideHistoryService(this);

        String driverName = getIntent().getStringExtra("userName");
        driverEmail = getIntent().getStringExtra("userEmail");

        if (driverName != null && !driverName.isEmpty()) {
            tvWelcomeDriver.setText("Welcome, " + driverName + "!");
        } else {
            tvWelcomeDriver.setText("Welcome to RideGo Driver!");
        }

        loadDriverEarnings();
    }

    private void loadDriverEarnings() {
        if (driverEmail == null || driverEmail.trim().isEmpty()) {
            tvDriverEarnings.setText("₹0.00");
            return;
        }

        try {
            double earnings =
                    rideHistoryService.getDriverEarnings(driverEmail);

            tvDriverEarnings.setText(
                    String.format(Locale.getDefault(), "₹%.2f", earnings)
            );

        } catch (Exception e) {
            Toast.makeText(this,
                    "Unable to load earnings.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (rideHistoryService != null && tvDriverEarnings != null) {
            loadDriverEarnings();
        }
    }
}
