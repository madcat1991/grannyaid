package com.example.grannyaid;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "GrannyAidPrefs";
    private static final String KEY_BLUETOOTH = "bluetooth";
    private static final String KEY_WIFI = "wifi";
    private static final String KEY_MOBILE_NETWORK = "mobile_network";
    private static final String KEY_SOUND_VOLUME = "sound_volume";
    private static final String KEY_EARPIECE_VOLUME = "earpiece_volume";

    private final SharedPreferences preferences;

    public SettingsManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSettings(boolean bluetooth, boolean wifi,
                             boolean mobileNetwork, int soundVolume, int earpieceVolume) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_BLUETOOTH, bluetooth);
        editor.putBoolean(KEY_WIFI, wifi);
        editor.putBoolean(KEY_MOBILE_NETWORK, mobileNetwork);
        editor.putInt(KEY_SOUND_VOLUME, soundVolume);
        editor.putInt(KEY_EARPIECE_VOLUME, earpieceVolume);
        editor.apply();
    }


    public boolean getBluetooth() {
        return preferences.getBoolean(KEY_BLUETOOTH, true);
    }

    public boolean getWifi() {
        return preferences.getBoolean(KEY_WIFI, true);
    }

    public boolean getMobileNetwork() {
        return preferences.getBoolean(KEY_MOBILE_NETWORK, true);
    }

    public int getSoundVolume() {
        return preferences.getInt(KEY_SOUND_VOLUME, 70);
    }

    public int getEarpieceVolume() {
        return preferences.getInt(KEY_EARPIECE_VOLUME, 70);
    }
}