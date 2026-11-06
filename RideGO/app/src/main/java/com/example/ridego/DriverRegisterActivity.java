
package com.example.ridego;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ridego.model.Driver;
import com.example.ridego.service.AuthService;

public class DriverRegisterActivity extends AppCompatActivity {

    EditText etDriverName, etDriverEmail, etDriverPhone;
    EditText etLicense, etVehicleNumber;
    EditText etDriverPassword, etDriverConfirmPassword;

    Spinner spVehicleType;
    Button btnDriverRegister;
    TextView tvDriverGoLogin;

    AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        authService = new AuthService(this);

        etDriverName = findViewById(R.id.etDriverName);
        etDriverEmail = findViewById(R.id.etDriverEmail);
        etDriverPhone = findViewById(R.id.etDriverPhone);
        etLicense = findViewById(R.id.etLicense);
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        etDriverPassword = findViewById(R.id.etDriverPassword);
        etDriverConfirmPassword =
                findViewById(R.id.etDriverConfirmPassword);

        spVehicleType = findViewById(R.id.spVehicleType);
        btnDriverRegister = findViewById(R.id.btnDriverRegister);
        tvDriverGoLogin = findViewById(R.id.tvDriverGoLogin);

        String[] vehicles = {"Auto", "Mini", "Sedan"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                vehicles
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spVehicleType.setAdapter(adapter);

        btnDriverRegister.setOnClickListener(v -> {

            String name = etDriverName.getText().toString().trim();
            String email = etDriverEmail.getText().toString().trim();
            String phone = etDriverPhone.getText().toString().trim();
            String license = etLicense.getText().toString().trim();
            String vehicleNumber =
                    etVehicleNumber.getText().toString().trim();

            String vehicleType =
                    spVehicleType.getSelectedItem().toString();

            String password =
                    etDriverPassword.getText().toString();

            String confirmPassword =
                    etDriverConfirmPassword.getText().toString();

            if (name.isEmpty() || email.isEmpty()
                    || phone.isEmpty() || license.isEmpty()
                    || vehicleNumber.isEmpty() || password.isEmpty()
                    || confirmPassword.isEmpty()) {

                Toast.makeText(this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();

            } else if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches()) {

                etDriverEmail.setError("Enter a valid email");

            } else if (!phone.matches("[0-9]{10}")) {

                etDriverPhone.setError(
                        "Enter valid 10-digit number");

            } else if (password.length() < 6) {

                etDriverPassword.setError(
                        "Password must contain at least 6 characters");

            } else if (!password.equals(confirmPassword)) {

                etDriverConfirmPassword.setError(
                        "Passwords do not match");

            } else {

                registerDriver(name, email, phone,
                        license, vehicleNumber,
                        vehicleType, password);
            }
        });

        tvDriverGoLogin.setOnClickListener(v -> finish());
    }

    private void registerDriver(
            String name, String email, String phone,
            String license, String vehicleNumber,
            String vehicleType, String password) {

        try {

            String passwordHash =
                    authService.createPasswordHash(password);

            Driver driver = new Driver(
                    name, email, phone, passwordHash,
                    license, vehicleNumber, vehicleType);

            boolean registered =
                    authService.registerDriver(driver);

            if (registered) {

                Toast.makeText(this,
                        "Driver registered successfully!",
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
