
package com.example.ridego;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ridego.service.AuthService;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegister, tvDriverRegister, tvForgot;

    AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authService = new AuthService(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvDriverRegister = findViewById(R.id.tvDriverRegister);
        tvForgot = findViewById(R.id.tvForgot);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Please enter email and password",
                        Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RiderRegisterActivity.class);
            startActivity(intent);
        });

        tvDriverRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    DriverRegisterActivity.class);
            startActivity(intent);
        });

        tvForgot.setOnClickListener(v ->
                Toast.makeText(this,
                        "Password recovery coming soon!",
                        Toast.LENGTH_SHORT).show());
    }

    private void loginUser(String email, String password) {

        try {
            JSONObject rider = authService.login(
                    email, password, "Rider");

            if (rider != null) {
                openRiderHome(rider);
                return;
            }

            JSONObject driver = authService.login(
                    email, password, "Driver");

            if (driver != null) {
                openDriverHome(
                        driver.getString("name"),
                        driver.getString("email")
                );
                return;
            }

            Toast.makeText(this,
                    "Account not found or incorrect password",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this,
                    "Login failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openRiderHome(JSONObject rider) {

        Intent intent = new Intent(
                MainActivity.this,
                RiderHomeActivity.class
        );

        try {
            intent.putExtra("userName", rider.getString("name"));
            intent.putExtra("userEmail", rider.getString("email"));
            intent.putExtra("userPhone", rider.getString("phone"));
            intent.putExtra("passwordHash", rider.getString("passwordHash"));

            startActivity(intent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this,
                    "Unable to open Rider Home",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openDriverHome(String name, String email) {

        Intent intent = new Intent(
                MainActivity.this,
                DriverHomeActivity.class
        );

        intent.putExtra("userName", name);
        intent.putExtra("userEmail", email);

        startActivity(intent);
        finish();
    }
}
