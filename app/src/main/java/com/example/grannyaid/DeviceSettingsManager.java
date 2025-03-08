package com.example.grannyaid;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresPermission;

public class DeviceSettingsManager {
    private static final String TAG = "DeviceSettingsManager";
    
    private final Context context;
    private final AudioManager audioManager;
    private final WifiManager wifiManager;
    
    public DeviceSettingsManager(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }
    
    
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public boolean setBluetooth(boolean enable) {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Log.e(TAG, "Bluetooth not supported on this device");
                return false;
            }
            
            if (enable) {
                return bluetoothAdapter.enable();
            } else {
                return bluetoothAdapter.disable();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to set Bluetooth: " + e.getMessage());
            return false;
        }
    }
    
    public boolean setWifi(boolean enable) {
        try {
            // On Android 10 (Q) and above, apps cannot enable/disable WiFi directly
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // First show guidance dialog, BEFORE opening WiFi settings
                new AlertDialog.Builder(context)
                        .setTitle(R.string.wifi_guide_title)
                        .setMessage(context.getString(enable ? 
                                R.string.wifi_guide_enable : 
                                R.string.wifi_guide_disable))
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            // Only open WiFi settings after user has read instructions
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            Log.i(TAG, "Opened WiFi settings for manual adjustment on Android 10+");
                        })
                        .show();
                
                return false;
            } else {
                // On older Android versions, we can still control WiFi directly
                return wifiManager.setWifiEnabled(enable);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to set WiFi: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Opens mobile data settings since direct control is not possible on modern Android devices
     * 
     * @param enable The desired state (used for UI feedback only)
     * @return Always returns false to indicate programmatic setting wasn't possible
     */
    public boolean setMobileNetwork(boolean enable) {
        try {
            // First show guidance dialog BEFORE opening settings
            new AlertDialog.Builder(context)
                    .setTitle(R.string.mobile_data_guide_title)
                    .setMessage(context.getString(enable ? 
                            R.string.mobile_data_guide_enable : 
                            R.string.mobile_data_guide_disable))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        openMobileDataSettings();
                    })
                    .show();
            
            Log.i(TAG, "Showing mobile data guidance dialog");
            return false; // Return false because the setting wasn't automatically applied
        } catch (Exception e) {
            Log.e(TAG, "Failed to show mobile network dialog: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Helper method to open the correct mobile data settings for different Android versions
     */
    private void openMobileDataSettings() {
        try {
            // Try different intents to open data settings - these vary by Android version and manufacturer
            Intent intent;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+, use the network & internet settings
                intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                // For Android 9 (Pie), use a more compatible setting
                intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                // Fallback in case the above doesn't work on some Android 9 devices
                if (!isIntentResolvable(intent)) {
                    intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                }
            } else {
                // For older Android versions
                intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            }
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
            Log.i(TAG, "Opened Mobile Data settings for manual adjustment");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open mobile network settings: " + e.getMessage(), e);
            // Try a fallback intent if the first one failed
            try {
                // Show the fallback guidance dialog
                new AlertDialog.Builder(context)
                        .setTitle(R.string.mobile_data_guide_title)
                        .setMessage(R.string.mobile_data_guide_fallback)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            Intent fallbackIntent = new Intent(Settings.ACTION_SETTINGS);
                            fallbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(fallbackIntent);
                        })
                        .show();
            } catch (Exception e2) {
                Log.e(TAG, "Fallback settings open also failed: " + e2.getMessage(), e2);
            }
        }
    }
    
    /**
     * Check if an intent can be resolved to an activity
     */
    private boolean isIntentResolvable(Intent intent) {
        return intent.resolveActivity(context.getPackageManager()) != null;
    }
    
    public boolean setSoundVolume(int volumePercent) {
        try {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int volume = (volumePercent * maxVolume) / 100;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to set sound volume: " + e.getMessage());
            return false;
        }
    }
    
    public boolean setEarpieceVolume(int volumePercent) {
        try {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            int volume = (volumePercent * maxVolume) / 100;
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to set earpiece volume: " + e.getMessage());
            return false;
        }
    }
    
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public boolean applyAllSettings(boolean bluetooth, boolean wifi,
                                    boolean mobileNetwork, int soundVolume, int earpieceVolume) {
        boolean success = true;
        
        if (!setBluetooth(bluetooth)) success = false;
        if (!setWifi(wifi)) success = false;
        if (!setMobileNetwork(mobileNetwork)) success = false;
        if (!setSoundVolume(soundVolume)) success = false;
        if (!setEarpieceVolume(earpieceVolume)) success = false;
        
        return success;
    }
}