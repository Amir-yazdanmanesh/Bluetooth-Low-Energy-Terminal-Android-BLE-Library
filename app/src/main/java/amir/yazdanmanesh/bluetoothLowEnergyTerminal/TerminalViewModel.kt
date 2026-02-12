package amir.yazdanmanesh.bluetoothLowEnergyTerminal

import amir.yazdanmanesh.ble_lib.TextUtil
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TerminalUiState(
    val hexEnabled: Boolean = false,
    val newline: String = TextUtil.newline_crlf,
    val initialStart: Boolean = true
)

class TerminalViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    fun toggleHex(): Boolean {
        val newHex = !_uiState.value.hexEnabled
        _uiState.update { it.copy(hexEnabled = newHex) }
        return newHex
    }

    fun setNewline(newline: String) {
        _uiState.update { it.copy(newline = newline) }
    }

    fun consumeInitialStart(): Boolean {
        val was = _uiState.value.initialStart
        if (was) {
            _uiState.update { it.copy(initialStart = false) }
        }
        return was
    }
}
