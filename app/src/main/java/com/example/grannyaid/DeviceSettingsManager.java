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
    
    /**
     * Check if airplane mode is currently enabled
     */
    public boolean isAirplaneModeEnabled() {
        try {
            return Settings.Global.getInt(context.getContentResolver(), 
                    Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        } catch (Exception e) {
            Log.e(TAG, "Failed to check airplane mode state: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if WiFi is currently enabled
     */
    public boolean isWifiEnabled() {
        try {
            return wifiManager.isWifiEnabled();
        } catch (Exception e) {
            Log.e(TAG, "Failed to check WiFi state: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if Bluetooth is currently enabled
     */
    public boolean isBluetoothEnabled() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Log.e(TAG, "Bluetooth not supported on this device");
                return false;
            }
            return bluetoothAdapter.isEnabled();
        } catch (Exception e) {
            Log.e(TAG, "Failed to check Bluetooth state: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if mobile data is currently enabled (best effort)
     * Since it's difficult to reliably check mobile data state on all Android versions,
     * we allow bypassing mobile data dialog if asked to do so.
     * This may not work on all devices due to API restrictions.
     */
    public boolean isMobileDataEnabled() {
        try {
            // Return true for desired setting to prevent dialogs
            // in the case where the setting is already correct
            if (context.getSharedPreferences("GrannyAidPrefs", Context.MODE_PRIVATE)
                    .getBoolean("skip_mobile_data_check", false)) {
                return true;
            }
            
            // This approach isn't reliable on all devices, but it's a best effort
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // On Android 10+, use ConnectivityManager with NetworkCapabilities
                android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);
                android.net.Network network = cm.getActiveNetwork();
                if (network == null) return false;
                
                android.net.NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return capabilities != null && 
                       capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR);
            } else {
                // For older versions, try to use reflection (may not work on all devices)
                android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);
                try {
                    java.lang.reflect.Method method = cm.getClass().getMethod("getMobileDataEnabled");
                    method.setAccessible(true);
                    return (Boolean) method.invoke(cm);
                } catch (Exception e) {
                    Log.e(TAG, "Reflection failed for mobile data check: " + e.getMessage(), e);
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to check mobile data state: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Set a flag to bypass mobile data check on next call
     * This is useful for airplane mode where mobile data cannot be enabled
     */
    public void setSkipMobileDataCheck(boolean skip) {
        try {
            context.getSharedPreferences("GrannyAidPrefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("skip_mobile_data_check", skip)
                .apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to set skip mobile data flag: " + e.getMessage(), e);
        }
    }
    
    /**
     * Opens airplane mode settings since direct control requires higher permissions on modern Android
     * 
     * @param enable The desired state (used for UI feedback only)
     * @return Always returns false to indicate programmatic setting wasn't possible
     */
    public boolean setAirplaneMode(boolean enable) {
        try {
            // First show guidance dialog BEFORE opening settings
            new AlertDialog.Builder(context)
                    .setTitle(R.string.airplane_guide_title)
                    .setMessage(context.getString(enable ? 
                            R.string.airplane_guide_enable : 
                            R.string.airplane_guide_disable))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        openAirplaneModeSettings();
                    })
                    .show();
            
            Log.i(TAG, "Showing airplane mode guidance dialog");
            return false; // Return false because the setting wasn't automatically applied
        } catch (Exception e) {
            Log.e(TAG, "Failed to show airplane mode dialog: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Helper method to open the airplane mode settings
     */
    private void openAirplaneModeSettings() {
        try {
            // There's no direct ACTION constant for Airplane Mode in Settings
            // So we use different intents based on Android version
            Intent intent;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14 (API 34)
                // For Android 14+, airplane mode is in "More connectivity options" rather than mobile network
                intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS); // This should open the network & internet settings
                // Update the guidance dialog to show Android 14 specific instructions
                new AlertDialog.Builder(context)
                        .setTitle(R.string.airplane_guide_title)
                        .setMessage(R.string.airplane_guide_android14)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            // Launch the intent only after showing instructions
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        })
                        .show();
                
                Log.i(TAG, "Showing Android 14 specific airplane mode guidance for 'More connectivity options'");
                return;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10-13, use network settings which contains airplane mode
                intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
            } else {
                // For older Android versions
                intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            }
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
            Log.i(TAG, "Opened settings for airplane mode adjustment");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open airplane mode settings: " + e.getMessage(), e);
            // Try a fallback intent if the first one failed
            try {
                // Show the fallback guidance dialog
                new AlertDialog.Builder(context)
                        .setTitle(R.string.airplane_guide_title)
                        .setMessage(R.string.airplane_guide_fallback)
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