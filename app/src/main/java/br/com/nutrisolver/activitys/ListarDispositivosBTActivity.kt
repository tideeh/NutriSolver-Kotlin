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
import br.com.nutrisolver.utils.ACTIVITY_REQUEST_LIGAR_BT
import br.com.nutrisolver.utils.INTENT_KEY_DEVICE_MAC_ADDRESS
import br.com.nutrisolver.utils.PERMISSION_REQUEST_ACCESS_FINE_LOCATION
import br.com.nutrisolver.utils.ToastUtil.show
import br.com.nutrisolver.utils.UserUtil.isLogged

class ListarDispositivosBTActivity : AppCompatActivity() {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var listviewBtDevices: ListView
    private lateinit var listviewNewBtDevices: ListView
    private lateinit var pairedDevicesArrayAdapter: ArrayAdapter<String>
    private lateinit var macAddressPairedList: ArrayList<String>
    private lateinit var macAddressNewList: ArrayList<String>
    private lateinit var newDevicesArrayAdapter: ArrayAdapter<String>
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
                        newDevicesArrayAdapter.add(device.name)
                        macAddressNewList.add(device.address)
                        Log.i("MY_BLUETOOTH", "receiver encontrou" + device.name)
                    }
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) { //setProgressBarIndeterminateVisibility(false);
                Log.i("MY_BLUETOOTH", "receiver finalizou")
                progressBar.visibility = View.GONE
                scanButton.visibility = View.VISIBLE
                //title = "Fim da procura"
                //if (newDevicesArrayAdapter.count == 0) { //String noDevices = "Nenhum dispositivo encontrado"
                    //ToastUtil.show(context, "Nenhum dispositivo encontrado", Toast.LENGTH_SHORT)
//NewDevicesArrayAdapter.add(noDevices);
                //}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_dispositivos_bt)

        newDevicesArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        progressBar = findViewById(R.id.progress_bar)

        configuraListview()
        // Register for broadcasts when a device is discovered
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        val filter2 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter2)

        configuraToolbar()
        // Initialize the button to perform device discovery
        scanButton = findViewById(R.id.button_scan)
        scanButton.setOnClickListener { v ->
            checkPermissionFirst()
            //doDiscovery()
            //v.visibility = View.GONE
        }
    }

    private fun checkPermissionFirst() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION
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

        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //show(this, "Permission granted.", Toast.LENGTH_SHORT)
                scanButton.visibility = View.GONE
                doDiscovery()
            } else {
                show(
                    this,
                    getString(R.string.permissao_necessaria_utilizar_funcionalidade),
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
            newDevicesArrayAdapter.clear()
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

        checkBtState()
        pairedDevicesArrayAdapter.clear()
        macAddressPairedList = ArrayList()
        val pairedDevices =
            bluetoothAdapter?.bondedDevices
        if (pairedDevices != null) {
            if (pairedDevices.size > 0) { // There are paired devices. Get the name and address of each paired device.
                for (device in pairedDevices) {
                    pairedDevicesArrayAdapter.add(device.name)
                    macAddressPairedList.add(device.address)
                    //String deviceName = device.getName();
//String deviceHardwareAddress = device.getAddress(); // MAC address
                }
            }
        }
    }

    private fun checkBtState() {
        if (bluetoothAdapter == null) {
            show(this, getString(R.string.dispositivo_nao_suporta_bluetooth), Toast.LENGTH_LONG)
            finish()
        } else {
            if (!((bluetoothAdapter as BluetoothAdapter).isEnabled)) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, ACTIVITY_REQUEST_LIGAR_BT)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_REQUEST_LIGAR_BT) {
            if (resultCode != Activity.RESULT_OK) { // bluetooth nao foi ativado
                show(this, getString(R.string.falha_ao_ativar_o_bluetooth), Toast.LENGTH_LONG)
                finish()
            }
        }
    }

    private fun configuraListview() {
        macAddressNewList = ArrayList()

        listviewNewBtDevices =
            findViewById<View>(R.id.listView_bt_new_devices) as ListView
        listviewNewBtDevices.adapter = newDevicesArrayAdapter
        listviewNewBtDevices.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                bluetoothAdapter?.cancelDiscovery()
                val macAddressSelected = macAddressNewList[position]
                val it = Intent()
                it.putExtra(INTENT_KEY_DEVICE_MAC_ADDRESS, macAddressSelected)
                setResult(Activity.RESULT_OK, it)
                finish()
            }

        listviewBtDevices =
            findViewById<View>(R.id.listView_bt_devices_paireds) as ListView
        pairedDevicesArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listviewBtDevices.adapter = pairedDevicesArrayAdapter
        listviewBtDevices.onItemClickListener =
            OnItemClickListener { adapterView, view, i, l ->
                bluetoothAdapter?.cancelDiscovery()
                val macAddressSelected = macAddressPairedList[i]
                val it = Intent()
                it.putExtra(INTENT_KEY_DEVICE_MAC_ADDRESS, macAddressSelected)
                setResult(Activity.RESULT_OK, it)
                finish()
            }
    }

    private fun configuraToolbar() { // adiciona a barra de tarefas na tela
        val myToolbar =
            findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(myToolbar)
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
