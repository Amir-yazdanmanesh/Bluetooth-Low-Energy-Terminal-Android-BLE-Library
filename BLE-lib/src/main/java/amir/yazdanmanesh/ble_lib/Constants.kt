package amir.yazdanmanesh.ble_lib

internal object Constants {
    // values have to be globally unique
    val INTENT_ACTION_DISCONNECT: String = BuildConfig.LIBRARY_PACKAGE_NAME + ".Disconnect"
    val NOTIFICATION_CHANNEL: String = BuildConfig.LIBRARY_PACKAGE_NAME + ".Channel"
    val INTENT_CLASS_MAIN_ACTIVITY: String = BuildConfig.LIBRARY_PACKAGE_NAME + ".MainActivity"

    // values have to be unique within each app
    const val NOTIFY_MANAGER_START_FOREGROUND_SERVICE: Int = 1001
}
