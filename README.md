# Bluetooth Low Energy Terminal
This Android app provides a line-oriented terminal / console for Bluetooth LE (4.x) devices implementing a custom serial profile
For an overview on Android BLE communication see [Android Bluetooth LEOverview](https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview).

This app includes UUIDs for widely used serial profiles:
- Nordic Semiconductor nRF51822  
- Texas Instruments CC254x
- Microchip RN4870/1
- Telit Bluemod


- Filtering, scanning, linking, reading, writing, notification subscription and cancellation in a simple way.
- Support custom scan rules  
- Support multi device connections  
- Support configuration timeout for conncet or operation  
## Requirements
- A device's location services can use Bluetooth to detect Bluetooth beacons and provide a more accurate location
- In Android 11 or lower, individual apps require location permissions to use BLE scanning
- From Android 12, the BLUETOOTH_SCAN, BLUETOOTH_ADVERTISE, and BLUETOOTH_CONNECT permissions can allow apps to scan for nearby devices without needing to request the location permission. For more information, see [New Bluetooth permissions in Android 12](https://developer.android.com/about/versions/12/features/bluetooth-permissions).
- My BLE library work on Android 5.0+ (API level 21+) .

## Preview
![](https://s23.picofile.com/file/8448095584/ezgif_com_gif_maker.gif) 

