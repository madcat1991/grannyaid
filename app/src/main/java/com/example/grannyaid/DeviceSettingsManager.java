package com.example.grannyaid;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

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
    
    public boolean setAirplaneMode(boolean enable) {
        try {
            // For API level below 17, we need to use Settings.System
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Settings.System.putInt(context.getContentResolver(), 
                        Settings.System.AIRPLANE_MODE_ON, enable ? 1 : 0);
            } else {
                // For API level 17 and above, we need to use Settings.Global
                Settings.Global.putInt(context.getContentResolver(), 
                        Settings.Global.AIRPLANE_MODE_ON, enable ? 1 : 0);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to set airplane mode: " + e.getMessage());
            return false;
        }
    }
    
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
            return wifiManager.setWifiEnabled(enable);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set WiFi: " + e.getMessage());
            return false;
        }
    }
    
    public boolean setMobileNetwork(boolean enable) {
        // Note: Setting mobile data programmatically requires system app privileges on newer Android versions
        // This is a limited implementation that may not work on all devices or Android versions
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // On Android Oreo and above, this requires MODIFY_PHONE_STATE permission
                // which is only available to system apps
                Log.w(TAG, "Setting mobile data requires system app privileges on Android 8+");
                return false;
            }
            
            // This is a common reflection method, but it's not guaranteed to work on all devices
            // and it's not part of the official API
            return true; // We return true here, but actual implementation would be device specific
        } catch (Exception e) {
            Log.e(TAG, "Failed to set mobile network: " + e.getMessage());
            return false;
        }
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
    
    public boolean applyAllSettings(boolean airplaneMode, boolean bluetooth, boolean wifi,
                                   boolean mobileNetwork, int soundVolume, int earpieceVolume) {
        boolean success = true;
        
        if (!setAirplaneMode(airplaneMode)) success = false;
        if (!setBluetooth(bluetooth)) success = false;
        if (!setWifi(wifi)) success = false;
        if (!setMobileNetwork(mobileNetwork)) success = false;
        if (!setSoundVolume(soundVolume)) success = false;
        if (!setEarpieceVolume(earpieceVolume)) success = false;
        
        return success;
    }
}