package com.example.grannyaid;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_SETTINGS = 1001;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1002;
    
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
        fixButton.setOnClickListener(v -> {
            if (hasRequiredPermissions()) {
                fixSettings();
            } else {
                showPermissionDialog();
            }
        });
        settingsButton.setOnClickListener(v -> openSettings());
        
        // Check for required permissions
        checkPermissions();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check permissions again on resume, in case user granted them in settings
        updateButtonState();
    }
    
    private boolean hasRequiredPermissions() {
        boolean hasWriteSettings = true;
        boolean hasBluetoothPermissions = true;
        
        // Check WRITE_SETTINGS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasWriteSettings = Settings.System.canWrite(this);
        }
        
        // Check BLUETOOTH_CONNECT permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasBluetoothPermissions = checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) 
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        
        return hasWriteSettings && hasBluetoothPermissions;
    }
    
    private void updateButtonState() {
        boolean hasPermissions = hasRequiredPermissions();
        fixButton.setEnabled(hasPermissions);
        
        // Change button appearance based on permission state
        if (!hasPermissions) {
            fixButton.setAlpha(0.5f);
            fixButton.setText(R.string.need_permission);
        } else {
            fixButton.setAlpha(1.0f);
            fixButton.setText(R.string.fix_it);
        }
    }
    
    private void checkPermissions() {
        // Check for WRITE_SETTINGS permission on Android 6.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                showPermissionDialog();
                return; // Handle one permission at a time
            }
        }
        
        // Check for BLUETOOTH_CONNECT permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != 
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_PERMISSIONS);
                return; // Handle one permission at a time
            }
        }
        
        // Update button state based on permissions
        updateButtonState();
    }
    
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.permission_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    requestWriteSettingsPermission();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    
    private void requestWriteSettingsPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        
        // Show instruction dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_instruction_title)
                .setMessage(R.string.permission_instruction_message)
                .setPositiveButton(R.string.go_to_settings, (dialog, which) -> {
                    startActivityForResult(intent, REQUEST_WRITE_SETTINGS);
                })
                .setCancelable(false)
                .show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Check if the user granted the permission
        if (requestCode == REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                    
                    // Continue checking other permissions
                    checkPermissions();
                } else {
                    // Show failed dialog with instructions
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.permission_not_granted)
                            .setMessage(R.string.permission_failed_instructions)
                            .setPositiveButton(R.string.try_again, (dialog, which) -> {
                                requestWriteSettingsPermission();
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.bluetooth_permission_granted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.bluetooth_permission_denied, Toast.LENGTH_SHORT).show();
            }
            
            // Continue to check other permissions or update UI
            updateButtonState();
        }
    }
    
    private void fixSettings() {
        try {
            Log.d("MainActivity", "Attempting to fix settings with values: " +
                    "Bluetooth=" + settingsManager.getBluetooth() +
                    ", Wifi=" + settingsManager.getWifi() + 
                    ", SoundVolume=" + settingsManager.getSoundVolume() +
                    ", EarpieceVolume=" + settingsManager.getEarpieceVolume());
            
            // Set audio settings - these are most likely to work
            boolean soundSuccess = deviceSettingsManager.setSoundVolume(settingsManager.getSoundVolume());
            boolean earpieceSuccess = deviceSettingsManager.setEarpieceVolume(settingsManager.getEarpieceVolume());
            
            // Set WiFi
            boolean wifiSuccess = deviceSettingsManager.setWifi(settingsManager.getWifi());
            
            // Only try Bluetooth if we have the permission
            boolean bluetoothSuccess = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == 
                        android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    bluetoothSuccess = deviceSettingsManager.setBluetooth(settingsManager.getBluetooth());
                }
            } else {
                // For older Android versions
                bluetoothSuccess = deviceSettingsManager.setBluetooth(settingsManager.getBluetooth());
            }
            
            Log.d("MainActivity", "Settings applied: " +
                    "Bluetooth=" + bluetoothSuccess +
                    ", Wifi=" + wifiSuccess + 
                    ", SoundVolume=" + soundSuccess +
                    ", EarpieceVolume=" + earpieceSuccess);
            
            // Define what counts as success (audio settings should always work)
            boolean success = soundSuccess && earpieceSuccess;
            boolean anySuccess = soundSuccess || earpieceSuccess || wifiSuccess || bluetoothSuccess;
            
            // Show appropriate success message
            if (success) {
                Toast.makeText(this, getString(R.string.settings_fixed), Toast.LENGTH_LONG).show();
                showSuccessAnimation();
            } else if (anySuccess) {
                Toast.makeText(this, getString(R.string.settings_partially_fixed), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.settings_not_fixed), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error fixing settings: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.error_fixing_settings), Toast.LENGTH_LONG).show();
        }
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