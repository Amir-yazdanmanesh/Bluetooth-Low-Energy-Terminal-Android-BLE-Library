package amir.yazdanmanesh.bluetoothLowEnergyTerminal

import amir.yazdanmanesh.ble_lib.SerialListener
import amir.yazdanmanesh.ble_lib.SerialService
import amir.yazdanmanesh.ble_lib.SerialSocket
import amir.yazdanmanesh.ble_lib.TextUtil
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import amir.yazdanmanesh.bluetoothLowEnergyTerminal.databinding.FragmentTerminalBinding

@SuppressLint("MissingPermission")
class TerminalFragment : Fragment(), ServiceConnection, SerialListener {

    private enum class Connected { False, Pending, True }

    private var _binding: FragmentTerminalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TerminalViewModel by viewModels()

    private var deviceAddress: String? = null
    private var service: SerialService? = null
    private var hexWatcher: TextUtil.HexWatcher? = null
    private var connected = Connected.False
    private var pendingNewline = false

    /*
     * Lifecycle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceAddress = requireArguments().getString("device")
    }

    override fun onDestroy() {
        if (connected != Connected.False) disconnect()
        requireActivity().stopService(Intent(activity, SerialService::class.java))
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        service?.attach(this)
            ?: requireActivity().startService(Intent(activity, SerialService::class.java))
    }

    override fun onStop() {
        if (service != null && !requireActivity().isChangingConfigurations) service!!.detach()
        super.onStop()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().bindService(
            Intent(activity, SerialService::class.java),
            this,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onDetach() {
        try {
            requireActivity().unbindService(this)
        } catch (_: Exception) {
        }
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.uiState.value.initialStart && service != null) {
            viewModel.consumeInitialStart()
            requireActivity().runOnUiThread { connect() }
        }
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        service = (binder as SerialService.SerialBinder).getService()
        service!!.attach(this)
        if (viewModel.consumeInitialStart() && isResumed) {
            requireActivity().runOnUiThread { connect() }
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        service = null
    }

    /*
     * UI
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTerminalBinding.inflate(inflater, container, false)

        val hexEnabled = viewModel.uiState.value.hexEnabled
        binding.receiveText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorRecieveText))
        binding.receiveText.movementMethod = ScrollingMovementMethod.getInstance()

        hexWatcher = TextUtil.HexWatcher(binding.sendText)
        hexWatcher!!.enable(hexEnabled)
        binding.sendText.addTextChangedListener(hexWatcher)
        binding.sendText.hint = if (hexEnabled) "HEX mode" else ""

        binding.sendBtn.setOnClickListener {
            send(binding.sendText.text.toString())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_terminal, menu)
                menu.findItem(R.id.hex).isChecked = viewModel.uiState.value.hexEnabled
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.clear -> {
                        binding.receiveText.text = ""
                        true
                    }
                    R.id.newline -> {
                        val newlineNames = resources.getStringArray(R.array.newline_names)
                        val newlineValues = resources.getStringArray(R.array.newline_values)
                        val pos = newlineValues.indexOf(viewModel.uiState.value.newline)
                        AlertDialog.Builder(requireContext())
                            .setTitle("Newline")
                            .setSingleChoiceItems(newlineNames, pos) { dialog, item ->
                                viewModel.setNewline(newlineValues[item])
                                dialog.dismiss()
                            }
                            .create().show()
                        true
                    }
                    R.id.hex -> {
                        val hexEnabled = viewModel.toggleHex()
                        binding.sendText.text = null
                        hexWatcher!!.enable(hexEnabled)
                        binding.sendText.hint = if (hexEnabled) "HEX mode" else ""
                        menuItem.isChecked = hexEnabled
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
     * Serial + UI
     */
    private fun connect() {
        try {
            val btManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val device = btManager.adapter.getRemoteDevice(deviceAddress)
            status("connecting...")
            connected = Connected.Pending
            val socket = SerialSocket(requireActivity().applicationContext, device)
            service!!.connect(socket)
        } catch (e: Exception) {
            onSerialConnectError(e)
        }
    }

    private fun disconnect() {
        connected = Connected.False
        service!!.disconnect()
    }

    private fun send(str: String) {
        if (connected != Connected.True) {
            Toast.makeText(activity, "not connected", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val state = viewModel.uiState.value
            val msg: String
            val data: ByteArray
            if (state.hexEnabled) {
                val sb = StringBuilder()
                TextUtil.toHexString(sb, TextUtil.fromHexString(str))
                TextUtil.toHexString(sb, state.newline.toByteArray())
                msg = sb.toString()
                data = TextUtil.fromHexString(msg)
            } else {
                msg = str
                data = (str + state.newline).toByteArray()
            }
            val spn = SpannableStringBuilder("$msg\n")
            spn.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorSendText)),
                0, spn.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.receiveText.append(spn)
            service!!.write(data)
        } catch (e: Exception) {
            onSerialIoError(e)
        }
    }

    private fun receive(data: ByteArray) {
        val state = viewModel.uiState.value
        if (state.hexEnabled) {
            binding.receiveText.append(TextUtil.toHexString(data) + '\n')
        } else {
            var msg = String(data)
            if (state.newline == TextUtil.newline_crlf && msg.isNotEmpty()) {
                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf)
                if (pendingNewline && msg[0] == '\n') {
                    val edt = binding.receiveText.editableText
                    if (edt != null && edt.length > 1) edt.replace(edt.length - 2, edt.length, "")
                }
                pendingNewline = msg[msg.length - 1] == '\r'
            }
            binding.receiveText.append(TextUtil.toCaretString(msg, state.newline.isNotEmpty()))
        }
    }

    private fun status(str: String) {
        val spn = SpannableStringBuilder("$str\n")
        spn.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorStatusText)),
            0, spn.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.receiveText.append(spn)
    }

    /*
     * SerialListener
     */
    override fun onSerialConnect() {
        status("connected")
        connected = Connected.True
    }

    override fun onSerialConnectError(e: Exception) {
        status("connection failed: " + e.message)
        disconnect()
    }

    override fun onSerialRead(data: ByteArray) {
        receive(data)
    }

    override fun onSerialIoError(e: Exception) {
        status("connection lost: " + e.message)
        disconnect()
    }
}
