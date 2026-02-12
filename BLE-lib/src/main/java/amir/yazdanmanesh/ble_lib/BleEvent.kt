package amir.yazdanmanesh.ble_lib

sealed class BleEvent {
    object Connected : BleEvent()
    data class ConnectError(val error: Exception) : BleEvent()
    data class DataReceived(val data: ByteArray) : BleEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is DataReceived) return false
            return data.contentEquals(other.data)
        }
        override fun hashCode(): Int = data.contentHashCode()
    }
    data class IoError(val error: Exception) : BleEvent()
}
