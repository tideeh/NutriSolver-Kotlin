package br.com.nutrisolver.activitys

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.nutrisolver.R
import br.com.nutrisolver.utils.ToastUtil
import br.com.nutrisolver.utils.ToastUtil.show
import br.com.nutrisolver.utils.UserUtil.isLogged

class ListarDispositivosBTActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var listView_bt_devices: ListView
    private lateinit var listView_new_devices: ListView
    private lateinit var pairedDevicesArrayAdapter: ArrayAdapter<String>
    private lateinit var mac_address_paired_list: ArrayList<String>
    private lateinit var mac_address_new_list: ArrayList<String>
    private lateinit var NewDevicesArrayAdapter: ArrayAdapter<String>
    private lateinit var progressBar: ProgressBar
    private lateinit var scanButton: Button
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val action = intent.action
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                Log.i("MY_BLUETOOTH", "receiver entrou")
                // Get the BluetoothDevice object from the Intent
                val device = intent.getParcelableExtra<BluetoothDevice>(
                    BluetoothDevice.EXTRA_DEVICE
                )
                if (device != null) {
                    if (device.bondState != BluetoothDevice.BOND_BONDED) {
                        NewDevicesArrayAdapter.add(device.name)
                        mac_address_new_list.add(device.address)
                        Log.i("MY_BLUETOOTH", "receiver encontrou" + device.name)
                    }
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) { //setProgressBarIndeterminateVisibility(false);
                Log.i("MY_BLUETOOTH", "receiver finalizou")
                progressBar.visibility = View.GONE
                scanButton.visibility = View.VISIBLE
                title = "Fim da procura"
                if (NewDevicesArrayAdapter.count == 0) { //String noDevices = "Nenhum dispositivo encontrado"
                    //ToastUtil.show(context, "Nenhum dispositivo encontrado", Toast.LENGTH_SHORT)
//NewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_dispositivos_bt)

        NewDevicesArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        progressBar = findViewById(R.id.progress_bar)

        configura_listView()
        // Register for broadcasts when a device is discovered
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        val filter2 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter2)

        configura_toolbar()
        // Initialize the button to perform device discovery
        scanButton = findViewById(R.id.button_scan)
        scanButton.setOnClickListener { v ->
            check_permission_first()
            //doDiscovery()
            //v.visibility = View.GONE
        }
    }

    private fun check_permission_first() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1003
            )

        } else {
            //ToastUtil.show(this, "Permission already granted.", Toast.LENGTH_SHORT)
            scanButton.visibility = View.GONE
            doDiscovery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1003) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                ToastUtil.show(this, "Permission granted.", Toast.LENGTH_SHORT)
                scanButton.visibility = View.GONE
                doDiscovery()
            } else {
                ToastUtil.show(
                    this,
                    "Permissão necessária para utilizar esta funcionalidade",
                    Toast.LENGTH_SHORT
                )
                scanButton.visibility = View.VISIBLE
            }
        }
    }

    private fun doDiscovery() { // Indicate scanning in the title
//setProgressBarIndeterminateVisibility(true);
//setTitle(R.string.scanning);
// Turn on sub-title for new devices
//findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
// If we're already discovering, stop it
        if (bluetoothAdapter != null) {
            if ((bluetoothAdapter as BluetoothAdapter).isDiscovering)
                (bluetoothAdapter as BluetoothAdapter).cancelDiscovery()
            // Request discover from BluetoothAdapter
            NewDevicesArrayAdapter.clear()
            progressBar.visibility = View.VISIBLE
            (bluetoothAdapter as BluetoothAdapter).startDiscovery()
        }
    }

    override fun onStart() {
        super.onStart()

        if (!isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        check_bt_state()
        pairedDevicesArrayAdapter.clear()
        mac_address_paired_list = ArrayList()
        val pairedDevices =
            bluetoothAdapter?.bondedDevices
        if (pairedDevices != null) {
            if (pairedDevices.size > 0) { // There are paired devices. Get the name and address of each paired device.
                for (device in pairedDevices) {
                    pairedDevicesArrayAdapter.add(device.name)
                    mac_address_paired_list.add(device.address)
                    //String deviceName = device.getName();
//String deviceHardwareAddress = device.getAddress(); // MAC address
                }
            }
        }
    }

    private fun check_bt_state() {
        if (bluetoothAdapter == null) {
            show(this, "Device does not support bluetooth", Toast.LENGTH_LONG)
            finish()
        } else {
            if (!((bluetoothAdapter as BluetoothAdapter).isEnabled)) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != Activity.RESULT_OK) { // bluetooth nao foi ativado
                show(this, "Falha ao ativar o bluetooth", Toast.LENGTH_LONG)
                finish()
            }
        }
    }

    private fun configura_listView() {
        mac_address_new_list = ArrayList()

        listView_new_devices =
            findViewById<View>(R.id.listView_bt_new_devices) as ListView
        listView_new_devices.adapter = NewDevicesArrayAdapter
        listView_new_devices.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                bluetoothAdapter?.cancelDiscovery()
                val mac_address_selected = mac_address_new_list[position]
                val it = Intent()
                it.putExtra("device_mac_address", mac_address_selected)
                setResult(Activity.RESULT_OK, it)
                finish()
            }
        listView_bt_devices =
            findViewById<View>(R.id.listView_bt_devices_paireds) as ListView
        pairedDevicesArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView_bt_devices.adapter = pairedDevicesArrayAdapter
        listView_bt_devices.onItemClickListener =
            OnItemClickListener { adapterView, view, i, l ->
                bluetoothAdapter?.cancelDiscovery()
                val mac_address_selected = mac_address_paired_list[i]
                val it = Intent()
                it.putExtra("device_mac_address", mac_address_selected)
                setResult(Activity.RESULT_OK, it)
                finish()
            }
    }

    private fun configura_toolbar() { // adiciona a barra de tarefas na tela
        val my_toolbar =
            findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(my_toolbar)
        // adiciona a seta de voltar na barra de tarefas
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure we're not doing discovery anymore
        bluetoothAdapter?.cancelDiscovery()
        // Unregister broadcast listeners
        unregisterReceiver(mReceiver)
    }
}
