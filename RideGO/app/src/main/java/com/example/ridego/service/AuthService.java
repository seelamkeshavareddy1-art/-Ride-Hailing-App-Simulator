
package com.example.ridego.service;

import android.content.Context;

import com.example.ridego.model.Rider;
import com.example.ridego.model.Driver;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class AuthService {

    private final Context context;

    public AuthService(Context context) {
        this.context = context.getApplicationContext();
    }

    private String getFileName(String role, String email) {
        String safeEmail = email.replaceAll(
                "[^a-zA-Z0-9]", "_");

        return role.toLowerCase() + "_" + safeEmail + ".json";
    }

    private String hashPassword(String password) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();

        for (byte b : hash) {
            result.append(String.format("%02x", b & 0xff));
        }

        return result.toString();
    }

    private boolean fileExists(String fileName) {
        for (String file : context.fileList()) {
            if (file.equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    private void saveData(String fileName, JSONObject data)
            throws Exception {

        try (FileOutputStream fos =
                     context.openFileOutput(
                             fileName, Context.MODE_PRIVATE);
             OutputStreamWriter writer =
                     new OutputStreamWriter(
                             fos, StandardCharsets.UTF_8)) {

            writer.write(data.toString());
            writer.flush();
        }
    }

    private JSONObject readData(String fileName)
            throws Exception {

        try (FileInputStream fis =
                     context.openFileInput(fileName);
             InputStreamReader reader =
                     new InputStreamReader(
                             fis, StandardCharsets.UTF_8)) {

            StringBuilder data = new StringBuilder();
            char[] buffer = new char[1024];
            int length;

            while ((length = reader.read(buffer)) != -1) {
                data.append(buffer, 0, length);
            }

            return new JSONObject(data.toString());
        }
    }

    public boolean registerRider(Rider rider)
            throws Exception {

        String fileName = getFileName(
                "Rider", rider.getEmail());

        if (fileExists(fileName)) {
            return false;
        }

        JSONObject data = new JSONObject();

        data.put("name", rider.getName());
        data.put("email", rider.getEmail());
        data.put("phone", rider.getPhone());
        data.put("passwordHash",
                rider.getPasswordHash());
        data.put("role", "Rider");

        saveData(fileName, data);
        return true;
    }

    public boolean registerDriver(Driver driver)
            throws Exception {

        String fileName = getFileName(
                "Driver", driver.getEmail());

        if (fileExists(fileName)) {
            return false;
        }

        JSONObject data = new JSONObject();

        data.put("name", driver.getName());
        data.put("email", driver.getEmail());
        data.put("phone", driver.getPhone());
        data.put("passwordHash",
                driver.getPasswordHash());
        data.put("license", driver.getLicense());
        data.put("vehicleNumber",
                driver.getVehicleNumber());
        data.put("vehicleType",
                driver.getVehicleType());
        data.put("role", "Driver");

        saveData(fileName, data);
        return true;
    }

    public JSONObject login(
            String email, String password, String role)
            throws Exception {

        String fileName = getFileName(role, email);

        if (!fileExists(fileName)) {
            return null;
        }

        JSONObject data = readData(fileName);

        String savedHash = data.getString("passwordHash");
        String enteredHash = hashPassword(password);

        if (savedHash.equals(enteredHash)) {
            return data;
        }

        return null;
    }

    public String createPasswordHash(String password)
            throws Exception {
        return hashPassword(password);
    }

    public java.util.List<Driver> getAllDrivers() throws Exception {

        java.util.List<Driver> drivers = new java.util.ArrayList<>();

        for (String fileName : context.fileList()) {

            if (!fileName.startsWith("driver_")
                    || !fileName.endsWith(".json")) {
                continue;
            }

            JSONObject data = readData(fileName);

            Driver driver = new Driver(
                    data.getString("name"),
                    data.getString("email"),
                    data.getString("phone"),
                    data.getString("passwordHash"),
                    data.getString("license"),
                    data.getString("vehicleNumber"),
                    data.getString("vehicleType")
            );

            // Assign demo locations BEFORE adding driver
            String vehicleType = driver.getVehicleType().trim();

            if (vehicleType.equalsIgnoreCase("Auto")) {
                driver.setCurrentLatitude(17.3850);
                driver.setCurrentLongitude(78.4867);

            } else if (vehicleType.equalsIgnoreCase("Mini")) {
                driver.setCurrentLatitude(17.4401);
                driver.setCurrentLongitude(78.3489);

            } else if (vehicleType.equalsIgnoreCase("Sedan")) {
                driver.setCurrentLatitude(17.4200);
                driver.setCurrentLongitude(78.3900);
            }

            drivers.add(driver);
        }

        return drivers;
    }
}
