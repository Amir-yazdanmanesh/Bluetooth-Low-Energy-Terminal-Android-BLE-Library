package amir.yazdanmanesh.bluetoothLowEnergyTerminal

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import amir.yazdanmanesh.bluetoothLowEnergyTerminal.databinding.FragmentDevicesBinding
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class DevicesFragment : Fragment() {

    private var _binding: FragmentDevicesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DevicesViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter

    private val bluetoothAdapter by lazy {
        val manager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startScan()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(getText(R.string.location_denied_title))
                .setMessage(getText(R.string.location_denied_message))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceAdapter = DeviceAdapter { device ->
            viewModel.stopScan()
            val args = Bundle()
            args.putString("device", device.address)
            val fragment = TerminalFragment()
            fragment.arguments = args
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment, fragment, "terminal")
                .addToBackStack(null)
                .commit()
        }

        binding.devicesRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_devices, menu)
                if (bluetoothAdapter == null) {
                    menu.findItem(R.id.bt_settings).isEnabled = false
                    menu.findItem(R.id.ble_scan).isEnabled = false
                } else if (!bluetoothAdapter!!.isEnabled) {
                    menu.findItem(R.id.ble_scan).isEnabled = false
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.ble_scan -> {
                        startScan()
                        true
                    }
                    R.id.ble_scan_stop -> {
                        viewModel.stopScan()
                        true
                    }
                    R.id.bt_settings -> {
                        startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    deviceAdapter.submitList(state.devices)
                    if (state.devices.isEmpty() && state.statusMessage.isNotEmpty()) {
                        binding.emptyText.visibility = View.VISIBLE
                        binding.devicesRecycler.visibility = View.GONE
                        binding.emptyText.text = state.statusMessage
                    } else {
                        binding.emptyText.visibility = View.GONE
                        binding.devicesRecycler.visibility = View.VISIBLE
                    }
                    requireActivity().invalidateOptionsMenu()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothAdapter == null) {
            viewModel.setStatusMessage("<bluetooth LE not supported>")
        } else if (!bluetoothAdapter!!.isEnabled) {
            viewModel.setStatusMessage("<bluetooth is disabled>")
        } else if (!viewModel.uiState.value.scanning) {
            viewModel.setStatusMessage("<use SCAN to refresh devices>")
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopScan()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startScan() {
        if (viewModel.uiState.value.scanning) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            val needed = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (needed.isNotEmpty()) {
                permissionLauncher.launch(needed.toTypedArray())
                return
            }
        } else {
            // Pre-Android 12
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.location_permission_title)
                    .setMessage(R.string.location_permission_message)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                    .show()
                return
            }
            // Check location services for pre-Android 12
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var locationEnabled = false
            try { locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (_: Exception) {}
            try { locationEnabled = locationEnabled || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) {}
            if (!locationEnabled) {
                viewModel.setStatusMessage("<location services disabled, scan may return fewer results>")
            }
        }

        viewModel.startScan(bluetoothAdapter?.bluetoothLeScanner)
    }
}
