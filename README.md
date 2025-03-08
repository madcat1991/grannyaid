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

1. **Prerequisites**:
   - Android Studio (Meerkat 2024.3.1+ recommended)
   - Java 11 or higher
   - Android SDK Platform 24 (Android 7.0) or higher

2. **Clone and open**:
   ```
   git clone https://github.com/yourusername/grannyaid.git
   ```
   Open in Android Studio: File > Open > select the project folder

3. **Build and run**:
   - Connect an Android device or set up an emulator (Tools > Device Manager)
   - Click the green "Run" button (or Shift+F10 / Ctrl+R)

4. **Generate APK for distribution**:
   - Build > Generate Signed Bundle/APK > APK
   - Follow the signing process to create the release APK
   - Install on the target device by enabling "Install from unknown sources"

## Important Permissions

The app requires special permissions to modify system settings. When prompted, please grant these permissions for the app to function correctly.

## Troubleshooting

### Common Build Issues

1. **Gradle/Java compatibility issues**:
   - Java 11+ required (check with `java -version`)
   - For Java 11: Gradle 8.0+ recommended
   - Fix: In gradle.properties, add `org.gradle.java.installations.auto-download=true`

2. **Sync or build failures**:
   - Try File > Invalidate Caches / Restart
   - Build > Clean Project, then Build > Rebuild Project
   - Check SDK Manager for missing components

3. **AndroidX issues**:
   - Ensure `android.useAndroidX=true` is in gradle.properties

### Runtime Issues

1. **Settings not changing**:
   - Grant all required permissions in Settings > Apps > GrannyAid > Permissions
   - Some settings require system privileges or aren't changeable on newer Android versions

2. **App crashes**:
   - Verify Android 7.0+ (Nougat) 
   - Check logcat for detailed errors

3. **Device-specific limitations**:
   - Samsung, Xiaomi, and other manufacturers may have custom restrictions on settings access