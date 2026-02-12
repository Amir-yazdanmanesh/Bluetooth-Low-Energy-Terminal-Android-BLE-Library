package amir.yazdanmanesh.ble_lib

interface SerialListener {
    fun onSerialConnect()
    fun onSerialConnectError(e: Exception)
    fun onSerialRead(data: ByteArray)
    fun onSerialIoError(e: Exception)
}
