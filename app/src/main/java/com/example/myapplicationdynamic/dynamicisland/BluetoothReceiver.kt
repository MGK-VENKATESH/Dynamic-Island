package com.example.myapplicationdynamic.dynamicisland



import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (isAudioDevice(it)) {
                        showDeviceConnected(context, it.name ?: "Bluetooth Device")
                    }
                }
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (isAudioDevice(it)) {
                        showDeviceDisconnected(context, it.name ?: "Bluetooth Device")
                    }
                }
            }
        }
    }

    private fun isAudioDevice(device: BluetoothDevice): Boolean {
        val deviceClass = device.bluetoothClass?.deviceClass ?: return false
        // Check if device is audio (headphones, earbuds, speakers)
        return deviceClass == 1048 || // Headphones
                deviceClass == 1056 || // Headset
                deviceClass == 1032    // Portable Audio
    }

    private fun showDeviceConnected(context: Context?, deviceName: String) {
        context?.let {
            val intent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
            intent.putExtra("type", "airpods")
            intent.putExtra("deviceName", deviceName)
            intent.putExtra("connected", true)
            it.sendBroadcast(intent)
        }
    }

    private fun showDeviceDisconnected(context: Context?, deviceName: String) {
        context?.let {
            val intent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
            intent.putExtra("type", "airpods")
            intent.putExtra("deviceName", deviceName)
            intent.putExtra("connected", false)
            it.sendBroadcast(intent)
        }
    }
}