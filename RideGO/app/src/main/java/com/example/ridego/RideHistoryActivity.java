package com.example.ridego;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ridego.service.RideHistoryService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RideHistoryActivity extends AppCompatActivity {

    private LinearLayout layoutRideHistory;
    private TextView tvHistoryEmpty;
    private RideHistoryService rideHistoryService;
    private String riderEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        layoutRideHistory = findViewById(R.id.layoutRideHistory);
        tvHistoryEmpty = findViewById(R.id.tvHistoryEmpty);

        rideHistoryService = new RideHistoryService(this);

        riderEmail = getIntent().getStringExtra("userEmail");

        if (riderEmail == null || riderEmail.trim().isEmpty()) {
            Toast.makeText(this,
                    "Rider email not found.",
                    Toast.LENGTH_SHORT).show();
            tvHistoryEmpty.setVisibility(TextView.VISIBLE);
            return;
        }

        loadRideHistory(riderEmail);
    }

    private void loadRideHistory(String riderEmail) {
        try {
            JSONArray rides =
                    rideHistoryService.getRiderHistory(riderEmail);

            layoutRideHistory.removeAllViews();

            if (rides.length() == 0) {
                tvHistoryEmpty.setVisibility(TextView.VISIBLE);
                return;
            }

            tvHistoryEmpty.setVisibility(TextView.GONE);

            for (int i = rides.length() - 1; i >= 0; i--) {
                JSONObject ride = rides.getJSONObject(i);
                addRideCard(ride);
            }

        } catch (Exception e) {
            Toast.makeText(this,
                    "Unable to load ride history.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void addRideCard(JSONObject ride) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 18, 20, 18);
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);

        addText(card, "Pickup: " +
                ride.optString("pickup", "Unknown"));

        addText(card, "Drop: " +
                ride.optString("drop", "Unknown"));

        addText(card, "Driver: " +
                ride.optString("driverName", "Not assigned"));

        addText(card, "Vehicle: " +
                ride.optString("vehicleType", "Unknown"));

        addText(card, String.format(Locale.getDefault(),
                "Distance: %.2f km",
                ride.optDouble("distance", 0)));

        addText(card, String.format(Locale.getDefault(),
                "Fare: ₹%.2f",
                ride.optDouble("fare", 0)));

        String status = ride.optString("status", "Unknown");
        addText(card, "Status: " + status);

        long timestamp = ride.optLong("timestamp", 0);

        if (timestamp > 0) {
            String date = new SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a",
                    Locale.getDefault()
            ).format(new Date(timestamp));

            addText(card, "Date: " + date);
        }

        // Show rating controls only for completed rides.
        if ("Completed".equalsIgnoreCase(status)) {

            int savedRating = ride.optInt("rating", 0);

            addText(card, savedRating > 0
                    ? "Your Rating: " + savedRating + "/5"
                    : "Rate your ride:");

            if (savedRating == 0) {

                LinearLayout ratingLayout =
                        new LinearLayout(this);

                ratingLayout.setOrientation(
                        LinearLayout.HORIZONTAL);

                for (int rating = 1; rating <= 5; rating++) {

                    final int selectedRating = rating;

                    Button ratingButton = new Button(this);
                    ratingButton.setText(String.valueOf(rating));

                    LinearLayout.LayoutParams buttonParams =
                            new LinearLayout.LayoutParams(
                                    0,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    1);

                    buttonParams.setMargins(3, 5, 3, 5);
                    ratingButton.setLayoutParams(buttonParams);

                    ratingButton.setOnClickListener(v -> {
                        saveRating(
                                ride.optString("rideId"),
                                selectedRating
                        );
                    });

                    ratingLayout.addView(ratingButton);
                }

                card.addView(ratingLayout);
            }
        }

        layoutRideHistory.addView(card);
    }

    private void saveRating(String rideId, int rating) {

        try {
            rideHistoryService.saveRideRating(
                    rideId,
                    riderEmail,
                    rating
            );

            Toast.makeText(this,
                    "Thank you for rating your ride!",
                    Toast.LENGTH_SHORT).show();

            loadRideHistory(riderEmail);

        } catch (Exception e) {
            Toast.makeText(this,
                    "Unable to save rating: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void addText(LinearLayout parent, String value) {

        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextColor(0xFFFFFFFF);
        textView.setTextSize(15);
        textView.setPadding(0, 5, 0, 5);
        textView.setGravity(Gravity.START);

        parent.addView(textView);
    }
}
