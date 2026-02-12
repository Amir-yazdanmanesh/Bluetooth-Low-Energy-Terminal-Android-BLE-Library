package amir.yazdanmanesh.bluetoothLowEnergyTerminal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import amir.yazdanmanesh.bluetoothLowEnergyTerminal.databinding.DeviceListItemBinding

@SuppressLint("MissingPermission")
class DeviceAdapter(
    private val onDeviceClick: (BluetoothDevice) -> Unit
) : ListAdapter<BluetoothDevice, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = DeviceListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeviceViewHolder(
        private val binding: DeviceListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BluetoothDevice) {
            binding.text1.text = if (device.name.isNullOrEmpty()) "<unnamed>" else device.name
            binding.text2.text = device.address
            binding.root.setOnClickListener { onDeviceClick(device) }
        }
    }

    private class DeviceDiffCallback : DiffUtil.ItemCallback<BluetoothDevice>() {
        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }
    }
}
