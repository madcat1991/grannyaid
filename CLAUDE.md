# CLAUDE.md - Reference for AI Coding Assistants

## Android Studio Commands
- Setup: Open project in Android Studio
- Sync Gradle: Click "Sync Now" when prompted, or go to File > Sync Project with Gradle Files
- Build: Click Build > Make Project (Ctrl+F9 / Cmd+F9)
- Run: Click Run > Run 'app' (Shift+F10 / Ctrl+R)
- Clean: Click Build > Clean Project
- Rebuild: Click Build > Rebuild Project
- Debug: Click Run > Debug 'app' (Shift+F9 / Ctrl+D)
- Device Manager: Tools > Device Manager (for emulator creation in Android Studio 2024.3.1+)
- Generate Signed APK: Build > Generate Signed Bundle/APK > APK
- Logcat: View > Tool Windows > Logcat (to view app logs)
- Layout Inspector: Tools > Layout Inspector (to debug UI issues)

## Code Style Guidelines
- Formatting: Use Android Studio's default formatting
- Imports: Sort imports alphabetically, group by external/internal
- Naming: 
  - camelCase for variables/methods
  - PascalCase for classes/interfaces
  - snake_case for resource IDs
  - ALL_CAPS for constants
- UI Design: Large text (24sp+), high contrast colors, simple layouts
- Error Handling: Use try/catch blocks with proper logging
- Folder structure: Standard Android project organization
- Accessibility: Ensure all UI elements have content descriptions
- Permissions: Request permissions at runtime with clear explanations
- Code organization: Each class should have a single responsibility