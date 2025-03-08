package com.example.grannyaid;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private SettingsManager settingsManager;
    
    private Switch bluetoothSwitch;
    private Switch wifiSwitch;
    private Switch mobileNetworkSwitch;
    private SeekBar soundVolumeSeekBar;
    private TextView soundVolumeText;
    private SeekBar earpieceVolumeSeekBar;
    private TextView earpieceVolumeText;
    private Button saveSettingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        settingsManager = new SettingsManager(this);
        
        // Initialize UI components
        bluetoothSwitch = findViewById(R.id.bluetoothSwitch);
        wifiSwitch = findViewById(R.id.wifiSwitch);
        mobileNetworkSwitch = findViewById(R.id.mobileNetworkSwitch);
        soundVolumeSeekBar = findViewById(R.id.soundVolumeSeekBar);
        soundVolumeText = findViewById(R.id.soundVolumeText);
        earpieceVolumeSeekBar = findViewById(R.id.earpieceVolumeSeekBar);
        earpieceVolumeText = findViewById(R.id.earpieceVolumeText);
        saveSettingsButton = findViewById(R.id.saveSettingsButton);
        
        // Load current settings
        loadSavedSettings();
        
        // Set up listeners
        setupListeners();
    }
    
    private void loadSavedSettings() {
        // Load settings from preferences
        bluetoothSwitch.setChecked(settingsManager.getBluetooth());
        wifiSwitch.setChecked(settingsManager.getWifi());
        mobileNetworkSwitch.setChecked(settingsManager.getMobileNetwork());
        
        int soundVolume = settingsManager.getSoundVolume();
        soundVolumeSeekBar.setProgress(soundVolume);
        soundVolumeText.setText(soundVolume + "%");
        
        int earpieceVolume = settingsManager.getEarpieceVolume();
        earpieceVolumeSeekBar.setProgress(earpieceVolume);
        earpieceVolumeText.setText(earpieceVolume + "%");
        
        // Update switch text
        updateSwitchText(bluetoothSwitch, bluetoothSwitch.isChecked());
        updateSwitchText(wifiSwitch, wifiSwitch.isChecked());
        updateSwitchText(mobileNetworkSwitch, mobileNetworkSwitch.isChecked());
    }
    
    private void setupListeners() {
        // Switch change listeners
        bluetoothSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> 
                updateSwitchText(bluetoothSwitch, isChecked));
        
        wifiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> 
                updateSwitchText(wifiSwitch, isChecked));
        
        mobileNetworkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> 
                updateSwitchText(mobileNetworkSwitch, isChecked));
        
        // SeekBar change listeners
        soundVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                soundVolumeText.setText(progress + "%");
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        earpieceVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                earpieceVolumeText.setText(progress + "%");
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Save button click listener
        saveSettingsButton.setOnClickListener(v -> saveSettings());
    }
    
    private void updateSwitchText(Switch switchView, boolean isChecked) {
        switchView.setText(isChecked ? "ON" : "OFF");
    }
    
    private void saveSettings() {
        // Save settings to preferences
        settingsManager.saveSettings(
                bluetoothSwitch.isChecked(),
                wifiSwitch.isChecked(),
                mobileNetworkSwitch.isChecked(),
                soundVolumeSeekBar.getProgress(),
                earpieceVolumeSeekBar.getProgress()
        );
        
        // Show success message
        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
        
        // Go back to main activity
        finish();
    }
}