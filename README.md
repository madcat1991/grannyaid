# GrannyAid

A simple Android application designed to help elderly people restore their phone settings with a single tap.

## Purpose

GrannyAid is designed for elderly users who may accidentally change critical phone settings and have difficulty navigating the phone's settings menu to restore them. With a single tap on the large "FIX IT" button, the app will automatically restore all settings to their predefined values.

## Features

- Simple, large-text interface designed for elderly users
- Large, prominent "FIX IT" button
- Settings for:
  - Airplane mode (on/off)
  - Bluetooth (on/off)
  - Wi-Fi (on/off)
  - Mobile network (on/off)
  - Sound volume (0-100%)
  - Earpiece volume (0-100%)
- Easy settings configuration

## How It Works

1. **Initial Setup**: A caregiver or family member sets up the app by configuring the desired settings (e.g., Wi-Fi should be ON, Airplane Mode should be OFF, etc.).
2. **Daily Use**: If the elderly person accidentally changes settings on their phone and experiences issues, they simply open the GrannyAid app and tap the large "FIX IT" button.
3. **Automatic Fix**: The app automatically restores all settings to their predefined values.

## Requirements

- Android 7.0 (Nougat) or higher
- System permissions for modifying settings

## Technical Notes

Some settings (like Mobile Network) may require specific permissions or system-level access that varies by Android version and device manufacturer. The app will attempt to change these settings but may not be successful on all devices.

## Building the App

To build the app:

1. Clone this repository
2. Open the project in Android Studio
3. Build and run on your device or emulator

## Important Permissions

The app requires special permissions to modify system settings. When prompted, please grant these permissions for the app to function correctly.