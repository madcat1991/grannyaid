package com.example.grannyaid;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_SETTINGS = 1001;
    
    private SettingsManager settingsManager;
    private DeviceSettingsManager deviceSettingsManager;
    
    private Button fixButton;
    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        settingsManager = new SettingsManager(this);
        deviceSettingsManager = new DeviceSettingsManager(this);
        
        // Initialize buttons
        fixButton = findViewById(R.id.fixButton);
        settingsButton = findViewById(R.id.settingsButton);
        
        // Set up click listeners
        fixButton.setOnClickListener(v -> fixSettings());
        settingsButton.setOnClickListener(v -> openSettings());
        
        // Check for required permissions
        checkPermissions();
    }
    
    private void checkPermissions() {
        // Check if we have WRITE_SETTINGS permission on Android 6.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                showPermissionDialog();
            }
        }
    }
    
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.permission_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_WRITE_SETTINGS);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Check if the user granted the permission
        if (requestCode == REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void fixSettings() {
        // Apply all saved settings
        boolean success = deviceSettingsManager.applyAllSettings(
                settingsManager.getAirplaneMode(),
                settingsManager.getBluetooth(),
                settingsManager.getWifi(),
                settingsManager.getMobileNetwork(),
                settingsManager.getSoundVolume(),
                settingsManager.getEarpieceVolume()
        );
        
        // Show success message with large font and simple words
        Toast.makeText(this, getString(R.string.settings_fixed), Toast.LENGTH_LONG).show();
        
        // You could also show a fullscreen message for a few seconds
        showSuccessAnimation();
    }
    
    private void showSuccessAnimation() {
        // This would be a good place to add a simple animation or a fullscreen success message
        // For now, we'll just rely on the Toast message
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}