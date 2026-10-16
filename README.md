# Trolyao — Android source project

Android Kotlin + Jetpack Compose project with dark rounded UI and RGB accents.

## Floating Menu (exactly three items)
- **Macro Runner** — sends one tap at the saved X/Y coordinate.
- **Multiply Button Runner** — press and hold the button to repeat taps; release to stop.
- **Tab** — opens the editor for X/Y, button size and opacity.

## Multiply behavior
The configured target interval is **2 ms**. Android shell command startup and device scheduling take longer than 2 ms in many cases, so this is a requested loop interval, not a guarantee of 500 taps per second. Keep the device/game responsive and release the button to stop the loop. Shizuku permission is required for touch commands.

## Main screen
- Shizuku permission status and request button.
- Overlay permission status and settings shortcut.
- Select an app/game.
- Foreground notification service toggle for the floating handle.

## Build APK from a phone using GitHub Actions
1. Extract this ZIP on the phone.
2. Upload the contents of `Trolyao-Complete` into repository `hosylong15071974-pixel/Trolyao` (upload the files/folders, not just the ZIP).
3. Open GitHub → repository → Actions → **Build Trolyao APK** → **Run workflow**.
4. When the run succeeds, download artifact `Trolyao-debug-apk` and extract `app-debug.apk`.

## Important status
This is an updated source package, not a claim that every Android 15/Oppo feature has been device-tested. Actual touch frequency and whether a target game accepts Shizuku shell input vary by device and app. The project does not use AccessibilityService. Features that still need further implementation/testing include automatic foreground-game detection, multiple independent Runner instances, draggable on-screen coordinate pin, full multi-step macro playback, swipe/long-press wiring to the floating menu, RGB animation, and per-game profile management.
