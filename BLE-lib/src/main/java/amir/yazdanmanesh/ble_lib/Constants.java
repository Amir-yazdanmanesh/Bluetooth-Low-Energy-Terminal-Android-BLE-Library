package amir.yazdanmanesh.ble_lib;

class Constants {

    // values have to be globally unique
    static final String INTENT_ACTION_DISCONNECT = BuildConfig.LIBRARY_PACKAGE_NAME + ".Disconnect";
    static final String NOTIFICATION_CHANNEL = BuildConfig.LIBRARY_PACKAGE_NAME + ".Channel";
    static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.LIBRARY_PACKAGE_NAME + ".MainActivity";

    // values have to be unique within each app
    static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;

    private Constants() {}
}
