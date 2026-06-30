# Hz Widget

A minimal Android application that allows you to quickly toggle your device's display refresh rate (e.g. 60Hz, 90Hz, 120Hz) directly from a home screen widget. The app scans for the available refresh rates supported by your display and adds them to the widget.

Built using Jetpack Compose for the main screen and Material You for the widget UI, with an extremely minimal footprint.

## Permissions Required
Because changing system settings (like refresh rate) requires elevated permissions, the app will request the **Modify System Settings** permission when you first open it. 
For some devices with strict OS skins, if that fails, you can manually grant the secure settings permission via ADB using your computer:

```sh
adb shell pm grant com.aistudio.hzwidget.rtywq android.permission.WRITE_SECURE_SETTINGS
```

## Features
- Material You Design.
- Absolute minimal APK size.
- Scans all device-supported refresh rates dynamically.
- Simple home-screen widget with quick toggles.

## License
MIT License. See `LICENSE` file for details.
