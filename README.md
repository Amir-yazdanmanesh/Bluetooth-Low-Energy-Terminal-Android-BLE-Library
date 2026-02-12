# Bluetooth Low Energy Terminal
[![](https://jitpack.io/v/Amir-yazdanmanesh/Bluetooth-Low-Energy-Terminal-Android-BLE-Library.svg)](https://jitpack.io/#Amir-yazdanmanesh/Bluetooth-Low-Energy-Terminal-Android-BLE-Library) [![android Status](https://img.shields.io/badge/platform-Android-yellow.svg)](https://www.android.com/)

This Android app provides a line-oriented terminal / console for Bluetooth LE (4.x) devices implementing a custom serial profile.
For an overview on Android BLE communication see [Android Bluetooth LE Overview](https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview).

This app includes UUIDs for widely used serial profiles:
- Nordic Semiconductor nRF51822
- Texas Instruments CC254x
- Microchip RN4870/1
- Telit Bluemod

## Features
- Filtering, scanning, linking, reading, writing, notification subscription and cancellation in a simple way
- Support custom scan rules
- Support multi device connections
- Support configuration timeout for connect or operation

## Tech Stack
| Component | Version |
|---|---|
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 1.9.24 |
| Gradle | 8.9 |
| compileSdk / targetSdk | 35 |
| minSdk | 21 |
| Java (toolchain) | 17 |

- **BLE-lib** — Pure Kotlin library, no UI dependencies
- **App** — ViewBinding, ViewModel + StateFlow, Coroutines, RecyclerView with ListAdapter/DiffUtil

## Requirements
- Android 5.0+ (API level 21+)
- On Android 11 or lower, location permission is required for BLE scanning
- On Android 12+, the app uses `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT` permissions instead of location. See [New Bluetooth permissions in Android 12](https://developer.android.com/about/versions/12/features/bluetooth-permissions)

## Install
### Step 1
Add JitPack to your settings.gradle (or root build.gradle) repositories:

```groovy
dependencyResolutionManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2
Add the dependency:

```groovy
dependencies {
    implementation 'com.github.Amir-yazdanmanesh:Bluetooth-Low-Energy-Terminal-Android-BLE-Library:1.0.6'
}
```

## Preview
![](https://s23.picofile.com/file/8448095584/ezgif_com_gif_maker.gif)
