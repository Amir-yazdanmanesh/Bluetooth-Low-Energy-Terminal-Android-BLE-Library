package amir.yazdanmanesh.bluetoothLowEnergyTerminal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DevicesUiState(
    val devices: List<BluetoothDevice> = emptyList(),
    val scanning: Boolean = false,
    val statusMessage: String = "initializing..."
)

@SuppressLint("MissingPermission")
class DevicesViewModel : ViewModel() {

    companion object {
        private const val LE_SCAN_PERIOD = 10000L
    }

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    private var scanner: BluetoothLeScanner? = null
    private var scanTimeoutJob: Job? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            _uiState.update { state ->
                if (state.devices.any { it.address == device.address }) {
                    state
                } else {
                    val newList = (state.devices + device).sortedWith(Comparator { a, b ->
                        compareDevices(a, b)
                    })
                    state.copy(devices = newList)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _uiState.update {
                it.copy(scanning = false, statusMessage = "<scan failed: error $errorCode>")
            }
        }
    }

    fun startScan(leScanner: BluetoothLeScanner?) {
        if (_uiState.value.scanning) return
        this.scanner = leScanner ?: return
        _uiState.update {
            it.copy(devices = emptyList(), scanning = true, statusMessage = "<scanning...>")
        }
        leScanner.startScan(scanCallback)
        scanTimeoutJob = viewModelScope.launch {
            delay(LE_SCAN_PERIOD)
            stopScan()
        }
    }

    fun stopScan() {
        if (!_uiState.value.scanning) return
        scanTimeoutJob?.cancel()
        scanTimeoutJob = null
        try {
            scanner?.stopScan(scanCallback)
        } catch (_: Exception) {
        }
        scanner = null
        _uiState.update {
            it.copy(
                scanning = false,
                statusMessage = if (it.devices.isEmpty()) "<no bluetooth devices found>" else ""
            )
        }
    }

    fun setStatusMessage(msg: String) {
        _uiState.update { it.copy(statusMessage = msg) }
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }

    private fun compareDevices(a: BluetoothDevice, b: BluetoothDevice): Int {
        val aValid = !a.name.isNullOrEmpty()
        val bValid = !b.name.isNullOrEmpty()
        if (aValid && bValid) {
            val ret = a.name.compareTo(b.name)
            return if (ret != 0) ret else a.address.compareTo(b.address)
        }
        if (aValid) return -1
        if (bValid) return 1
        return a.address.compareTo(b.address)
    }
}
