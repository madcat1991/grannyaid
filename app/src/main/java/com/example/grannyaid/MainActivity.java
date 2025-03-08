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
    
    // Tracks which settings need to be fixed
    private boolean needToFixAirplaneMode = false;
    private boolean needToFixBluetooth = false;
    private boolean needToFixWifi = false;
    private boolean needToFixMobileNetwork = false;
    
    // Tracks which settings have been successful
    private boolean soundSuccess = false;
    private boolean earpieceSuccess = false;
    private boolean airplaneModeSuccess = false;
    private boolean bluetoothSuccess = false;
    private boolean wifiSuccess = false;
    private boolean mobileNetworkSuccess = false;
    
    private void fixSettings() {
        try {
            // Start the fixing process
            isFixingSettings = true;
            
            Log.d("MainActivity", "Attempting to fix settings with values: " +
                    "Airplane Mode=" + settingsManager.getAirplaneMode() +
                    ", Bluetooth=" + settingsManager.getBluetooth() +
                    ", Wifi=" + settingsManager.getWifi() + 
                    ", SoundVolume=" + settingsManager.getSoundVolume() +
                    ", EarpieceVolume=" + settingsManager.getEarpieceVolume());
            
            // Reset all status flags
            resetSettingsStatus();
            
            // First, set audio settings - these are most likely to work and don't require dialogs
            soundSuccess = deviceSettingsManager.setSoundVolume(settingsManager.getSoundVolume());
            earpieceSuccess = deviceSettingsManager.setEarpieceVolume(settingsManager.getEarpieceVolume());
            
            // Determine which settings need to be fixed through dialogs only if they don't match desired state
            boolean wantAirplaneMode = settingsManager.getAirplaneMode();
            boolean currentAirplaneMode = deviceSettingsManager.isAirplaneModeEnabled();
            needToFixAirplaneMode = (wantAirplaneMode != currentAirplaneMode);
            Log.d("MainActivity", "Airplane mode: current=" + currentAirplaneMode + ", desired=" + wantAirplaneMode + ", need fix=" + needToFixAirplaneMode);
            
            // If airplane mode is enabled (or will be enabled), we should skip mobile data checks
            // because mobile data can't be enabled in airplane mode
            if (currentAirplaneMode || wantAirplaneMode) {
                deviceSettingsManager.setSkipMobileDataCheck(true);
                // Don't try to fix mobile network if airplane mode is enabled or needs to be enabled
                needToFixMobileNetwork = false;
                mobileNetworkSuccess = true; // Mark as success to avoid error messages
                Log.d("MainActivity", "Skipping mobile data check because of airplane mode");
            } else {
                deviceSettingsManager.setSkipMobileDataCheck(false);
                
                // Only check and potentially fix mobile network if airplane mode is not involved
                boolean wantMobileData = settingsManager.getMobileNetwork();
                boolean currentMobileData = deviceSettingsManager.isMobileDataEnabled();
                needToFixMobileNetwork = (wantMobileData != currentMobileData);
                Log.d("MainActivity", "Mobile data: current=" + currentMobileData + ", desired=" + wantMobileData + ", need fix=" + needToFixMobileNetwork);
            }
            
            boolean wantWifi = settingsManager.getWifi();
            boolean currentWifi = deviceSettingsManager.isWifiEnabled();
            needToFixWifi = (wantWifi != currentWifi);
            Log.d("MainActivity", "WiFi: current=" + currentWifi + ", desired=" + wantWifi + ", need fix=" + needToFixWifi);
            
            // Only apply Bluetooth changes if needed
            boolean wantBluetooth = settingsManager.getBluetooth();
            boolean currentBluetooth = deviceSettingsManager.isBluetoothEnabled();
            needToFixBluetooth = (wantBluetooth != currentBluetooth);
            Log.d("MainActivity", "Bluetooth: current=" + currentBluetooth + ", desired=" + wantBluetooth + ", need fix=" + needToFixBluetooth);
            
            if (!needToFixBluetooth) {
                // Already in correct state
                bluetoothSuccess = true;
                Log.d("MainActivity", "Bluetooth already in desired state, skipping");
            }
            // If it needs fixing, it will be handled in processNextSetting()
            
            // Check if all settings are already in desired state
            if (!needToFixAirplaneMode && !needToFixWifi && !needToFixMobileNetwork && 
                !needToFixBluetooth && soundSuccess && earpieceSuccess) {
                // All settings are already as desired, show success and skip the process
                Toast.makeText(this, getString(R.string.all_settings_already_correct), Toast.LENGTH_LONG).show();
                showSuccessAnimation();
                isFixingSettings = false;
                return;
            }
            
            // Start the sequential settings process
            processNextSetting();
            
        } catch (Exception e) {
            isFixingSettings = false; // Reset flag on error
            Log.e("MainActivity", "Error fixing settings: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.error_fixing_settings), Toast.LENGTH_LONG).show();
        }
    }
    
    private void resetSettingsStatus() {
        // Reset all success flags
        soundSuccess = false;
        earpieceSuccess = false;
        airplaneModeSuccess = false;
        bluetoothSuccess = false;
        wifiSuccess = false;
        mobileNetworkSuccess = false;
        
        // Reset which settings need fixing
        needToFixAirplaneMode = false;
        needToFixBluetooth = false;
        needToFixWifi = false;
        needToFixMobileNetwork = false;
    }
    
    private void processNextSetting() {
        // Process each setting one at a time, but only if needed
        // Priority order: 1. Airplane Mode 2. Mobile Network 3. WiFi 4. Bluetooth
        if (needToFixAirplaneMode) {
            needToFixAirplaneMode = false;
            // Show airplane mode dialog only when current state doesn't match desired state
            airplaneModeSuccess = deviceSettingsManager.setAirplaneMode(settingsManager.getAirplaneMode());
        } else if (needToFixMobileNetwork) {
            needToFixMobileNetwork = false;
            // Show mobile network dialog only when current state doesn't match desired state
            mobileNetworkSuccess = deviceSettingsManager.setMobileNetwork(settingsManager.getMobileNetwork());
        } else if (needToFixWifi) {
            needToFixWifi = false;
            // Show WiFi dialog only when current state doesn't match desired state
            wifiSuccess = deviceSettingsManager.setWifi(settingsManager.getWifi());
        } else if (needToFixBluetooth) {
            needToFixBluetooth = false;
            // Process Bluetooth fix if needed and permissions allow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == 
                        android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    bluetoothSuccess = deviceSettingsManager.setBluetooth(settingsManager.getBluetooth());
                }
            } else {
                // For older Android versions, direct control works
                bluetoothSuccess = deviceSettingsManager.setBluetooth(settingsManager.getBluetooth());
            }
            processNextSetting(); // Continue to next setting immediately since this doesn't show dialog
        } else {
            // All settings processed, show final status
            finishSettingsProcess();
        }
    }
    
    // Flag to track if we're in the process of fixing settings
    private boolean isFixingSettings = false;
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check permissions again on resume, in case user granted them in settings
        updateButtonState();
        
        // If we're in the process of fixing settings, continue to the next one
        if (isFixingSettings) {
            processNextSetting();
        }
    }
    
    private void finishSettingsProcess() {
        // Reset the flag since we're done
        isFixingSettings = false;
        
        // Log final results
        Log.d("MainActivity", "Settings applied: " +
                "Airplane Mode=" + airplaneModeSuccess +
                ", Bluetooth=" + bluetoothSuccess +
                ", Wifi=" + wifiSuccess + 
                ", Mobile Data=" + mobileNetworkSuccess +
                ", SoundVolume=" + soundSuccess +
                ", EarpieceVolume=" + earpieceSuccess);
        
        // Define what counts as success (audio settings should always work)
        boolean success = soundSuccess && earpieceSuccess;
        boolean anySuccess = soundSuccess || earpieceSuccess || wifiSuccess || 
                             bluetoothSuccess || airplaneModeSuccess || mobileNetworkSuccess;
        
        // Show appropriate success message
        if (success) {
            Toast.makeText(this, getString(R.string.settings_fixed), Toast.LENGTH_LONG).show();
            showSuccessAnimation();
        } else if (anySuccess) {
            Toast.makeText(this, getString(R.string.settings_partially_fixed), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.settings_not_fixed), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showSuccessAnimation() {
        // This would be a good place to add a simple animation or a fullscreen success message
        // For now, we'll just rely on the Toast message, but we could add a simple visual feedback
        fixButton.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction(() -> {
                fixButton.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200);
            });
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}