package com.example.tracker

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.CustomAdapter.onItemClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class Connect : Fragment() {

    private lateinit var v: View
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String
    private val REQUEST_ENABLE_BT = 1
    private var scanning = false
    private val handler = Handler()
    private val SCAN_PERIOD: Long = 10000
    private lateinit var m_pairedDevices: Set<BluetoothDevice>
    private lateinit var list1 : RecyclerView
    private lateinit var list2 : RecyclerView
    private lateinit var available : ArrayList<String>
    private lateinit var availableDev : ArrayList<BluetoothDevice>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_connect, container, false)

        val search: Button = v.findViewById(R.id.Search)
        available = ArrayList()
        availableDev = ArrayList()
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        list1 = v.findViewById(R.id.select_device_list)
        list2 = v.findViewById(R.id.available_device_list)

        if (btAdapter == null) {
            Toast.makeText(
                context,
                "Unfortunately, your device doesn't support Bluetooth.",
                Toast.LENGTH_LONG
            ).show()
        }

        if (btAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        search.setOnClickListener {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            btAdapter.startDiscovery()
            activity?.registerReceiver(receiver, filter)
            pairedDeviceList()
            val filter2 = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            activity?.registerReceiver(mBroadcastReceiver4, filter2)
           /* val adapter = CustomAdapter(available)
            val layoutManager = LinearLayoutManager(context)
            list2.layoutManager= layoutManager
            list2.adapter = adapter*/
        }
        return v
    }
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            println("action $action")
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if(!available.contains(device!!.name))
                        available.add(device!!.name)
                    availableDev.add(device)
                    Log.d(TAG, "onReceive: " + device.name + ": " + device.address)
                    val layoutManager = LinearLayoutManager(context)
                    list2.layoutManager= layoutManager
                    val adapter2 = CustomAdapter(available, object : onItemClickListener{
                        override fun onItemClick(position: Int) {
                            btAdapter.cancelDiscovery();

                            Log.d(TAG, "onItemClick: You Clicked on a device.");
                            val deviceName = available[position]
                            val deviceAddress = availableDev[position].address

                            Log.d(TAG, "onItemClick: deviceName = $deviceName");
                            Log.d(TAG, "onItemClick: deviceAddress = $deviceAddress");

                            //create the bond.
                            //NOTE: Requires API 17+? I think this is JellyBean
                            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                                Log.d(TAG, "Trying to pair with $deviceName");
                                availableDev[position].createBond();
                            }
                        }
                    })
                    list2.adapter = adapter2
                    adapter2.notifyDataSetChanged()
                    /*adapter2.setOnItemClickListener(object : CustomAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            ConnectThread(device)
                        }

                    })*/
                }
            }
            println("avail $available")
            //val adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, available) }
        }
    }
    private val mBroadcastReceiver4: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val mDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                //3 cases:
                //case1: bonded already
                if (mDevice!!.bondState == BluetoothDevice.BOND_BONDED) {
                    available.remove(mDevice.name)
                    availableDev.remove(mDevice)
                }
                //case2: creating a bone
                if (mDevice.bondState == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.")
                }
                //case3: breaking a bond
                if (mDevice.bondState == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.")
                }
            }
        }
    }

    private fun pairedDeviceList() {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        m_pairedDevices = btAdapter!!.bondedDevices
        val paired: ArrayList<String> = ArrayList()

        if (!m_pairedDevices.isEmpty()) {
            for (device: BluetoothDevice in m_pairedDevices) {
                paired.add(device.name)
                Log.i("device", "" + device)
            }
        } else {
            Toast.makeText(context, "No Paired devices have been found.", Toast.LENGTH_SHORT).show()
        }
        println("paired $paired")
        //val adapter = context?.let { RecyclerView.Adapter(it, android.R.layout.simple_list_item_1, paired) }
        val adapter = CustomAdapter(paired, object : onItemClickListener{
            override fun onItemClick(position: Int) {
            }
        })
        val layoutManager = LinearLayoutManager(context)
        list1.layoutManager= layoutManager
        list1.adapter = adapter

    }


   /* override fun onDestroy() {
        super.onDestroy()
        // Don't forget to unregister the ACTION_FOUND receiver.
        activity?.unregisterReceiver(receiver)
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (btAdapter!!.isEnabled) {
                        Toast.makeText(context, "Bluetooth has been enabled", Toast.LENGTH_SHORT)
                            .show()
                } else {
                        Toast.makeText(context, "Bluetooth has been disabled", Toast.LENGTH_SHORT)
                            .show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(
                    context,
                    "Bluetooth enabling has been canceled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        }

        override fun run() {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            // Cancel discovery because it otherwise slows down the connection.
            btAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                //manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }
}