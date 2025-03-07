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

### Detailed steps to build the app in Android Studio:

1. **Clone this repository**:
   ```
   git clone https://github.com/yourusername/grannyaid.git
   ```

2. **Open the project in Android Studio**:
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned repository folder and click "OK"
   - Wait for Gradle sync to complete

3. **Ensure you have the required SDK**:
   - Android Studio may prompt you to download required SDK components
   - Go to Tools > SDK Manager to verify you have Android SDK Platform 24 (Android 7.0) or higher
   - Install any missing components as prompted

4. **Configure a device or emulator**:
   - **Physical device**:
     - Enable Developer Options on your Android device (tap Build Number 7 times in Settings > About phone)
     - Enable USB debugging in Developer Options
     - Connect your device via USB
     - Allow USB debugging when prompted on your device
   - **Emulator**:
     - Go to Tools > AVD Manager
     - Click "Create Virtual Device"
     - Select a device definition (e.g., Pixel 4)
     - Select a system image with API level 24 or higher
     - Complete the configuration and click "Finish"

5. **Build and run the app**:
   - Click the green "Run" button in the toolbar
   - Select your target device from the list
   - Wait for the app to build and install
   - The app should launch automatically on your device/emulator

6. **Generate a release APK** (for distribution):
   - Go to Build > Generate Signed Bundle/APK
   - Select "APK" and click "Next"
   - Create or select a keystore for signing your app
   - Fill in the required information and click "Next"
   - Select release build type
   - Select destination folder and click "Finish"
   - The APK will be saved to the specified location

7. **Install the released APK** on your grandmother's device:
   - Enable "Install from unknown sources" in the device settings
   - Transfer the APK to the device via email, cloud storage, or USB
   - Browse to the APK on the device and tap to install

## Important Permissions

The app requires special permissions to modify system settings. When prompted, please grant these permissions for the app to function correctly.

## Troubleshooting

### Common Build Issues

1. **Gradle sync failed**:
   - Check your internet connection
   - Try File > Invalidate Caches / Restart
   - Make sure your Android Studio is up to date
   - Update Gradle plugin if prompted

2. **Missing SDK components**:
   - Go to Tools > SDK Manager and install any missing components
   - Accept licenses if prompted during installation

3. **Build errors related to resources**:
   - Make sure all resource files are properly formatted XML
   - Check that all referenced resources exist
   - Try Build > Clean Project followed by Build > Rebuild Project

4. **Gradle and Java compatibility issues**:
   - If you see "Cannot use @TaskAction annotation" or "incompatible Java/Gradle version" errors:
   - Check your Java version with `java -version` in terminal
   - For Java 21: Use Gradle 8.10+ and Android Gradle Plugin 8.2.0+
   - For Java 17: Use Gradle 8.0+ and Android Gradle Plugin 8.0.0+
   - For Java 11: Use Gradle 7.5+ and Android Gradle Plugin 7.4.0+
   - Update build.gradle files to use the appropriate syntax for your Gradle version
   - Try Build > Clean Project followed by Build > Rebuild Project

### Runtime Issues

1. **Settings don't change when "Fix It" is pressed**:
   - Check if you granted all required permissions
   - Some settings (especially mobile data) may require system app privileges
   - On newer Android versions, certain settings may be protected and cannot be changed programmatically

2. **App crashes on startup**:
   - Check logcat in Android Studio for detailed error messages
   - Ensure your device is running Android 7.0 or higher
   - Make sure all resources are properly defined

3. **Permission dialog doesn't appear**:
   - On some devices, you may need to manually grant permissions in Settings > Apps > GrannyAid > Permissions
   - Try reinstalling the app

### Device-Specific Issues

Some features may work differently on various manufacturers' devices due to custom Android implementations. Samsung, Xiaomi, and other manufacturers often modify Android settings access.