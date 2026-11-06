
package com.example.ridego;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ridego.model.Rider;
import com.example.ridego.service.AuthService;

public class RiderRegisterActivity extends AppCompatActivity {

    EditText etName, etRegEmail, etPhone;
    EditText etRegPassword, etConfirmPassword;

    Button btnRegister;
    TextView tvGoLogin;

    AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_register);

        authService = new AuthService(this);

        etName = findViewById(R.id.etName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etPhone);
        etRegPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String email = etRegEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etRegPassword.getText().toString();
            String confirmPassword =
                    etConfirmPassword.getText().toString();

            if (name.isEmpty() || email.isEmpty()
                    || phone.isEmpty() || password.isEmpty()
                    || confirmPassword.isEmpty()) {

                Toast.makeText(this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();

            } else if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches()) {

                etRegEmail.setError("Enter a valid email");

            } else if (!phone.matches("[0-9]{10}")) {

                etPhone.setError("Enter valid 10-digit number");

            } else if (password.length() < 6) {

                etRegPassword.setError(
                        "Password must contain at least 6 characters");

            } else if (!password.equals(confirmPassword)) {

                etConfirmPassword.setError(
                        "Passwords do not match");

            } else {

                registerRider(name, email, phone, password);
            }
        });

        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void registerRider(
            String name, String email,
            String phone, String password) {

        try {

            String passwordHash =
                    authService.createPasswordHash(password);

            Rider rider = new Rider(
                    name, email, phone, passwordHash);

            boolean registered =
                    authService.registerRider(rider);

            if (registered) {

                Toast.makeText(this,
                        "Rider registered successfully!",
                        Toast.LENGTH_LONG).show();

                finish();

            } else {

                Toast.makeText(this,
                        "Email already registered!",
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {

            Toast.makeText(this,
                    "Registration failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
