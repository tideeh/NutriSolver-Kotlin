package br.com.nutrisolver.activitys

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.com.nutrisolver.R
import br.com.nutrisolver.utils.*
import br.com.nutrisolver.utils.ToastUtil.show
import br.com.nutrisolver.utils.UserUtil.isLogged
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class ExecutarTeste1Activity : AppCompatActivity() {
    private val BT_MODULE_UUID = UUID.fromString(BT_MODULE_UUID_STRING)

    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    lateinit var textviewBtRead: TextView
    lateinit var textviewBtSend: TextView
    var startTime: Long = 0
    var timerHandler = Handler()
    private var deviceMacAddress: String = DEFAULT_STRING_VALUE
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private var threadParaConectar: ConnectThread? = null
    private var threadConectada: ConnectedThread? = null
    private var bluetoothIo: Handler? = null
    private lateinit var progressBar: ProgressBar
    private var estado = "0"
    private lateinit var editTextTempoParaExecucao: EditText
    private var tempoDoTeste = 10
    private lateinit var sharedPreferences: SharedPreferences

    var timerRunnable = object : Runnable {
        override fun run() {
            val millis: Long = System.currentTimeMillis() - startTime
            var seconds = (millis / 1000).toInt()
            seconds %= 60

            Log.i("MY_TESTE", ": $tempoDoTeste")

            findViewById<TextView>(R.id.teste_timer).text =
                (tempoDoTeste - seconds - 1).toString()

            timerHandler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_executar_teste1)

        progressBar = findViewById(R.id.progress_bar)
        editTextTempoParaExecucao = findViewById(R.id.tempo_para_execucao)
        textviewBtRead = findViewById(R.id.leitura_bluetooth_debug)
        textviewBtSend = findViewById(R.id.envio_bluetooth_debug)
        sharedPreferences = getSharedPreferences(SP_NOME, Context.MODE_PRIVATE)

        //bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        bluetoothIo = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_WHAT_WRITE -> {
                        val writeBuf = msg.obj as ByteArray
                        val writeMessage = String(writeBuf, 0, writeBuf.size)
                        Log.i(
                            "MY_BLUETOOTH",
                            "write: " + writeMessage + "; nBytes: " + writeBuf.size
                        )
                        val aux1 = "Enviado: $writeMessage"
                        textviewBtSend.text = aux1
                    }
                    MESSAGE_WHAT_READ -> {
                        val readBuf = msg.obj as ByteArray
                        // construct a string from the valid bytes in the buffer
                        val readMessage = String(readBuf, 0, msg.arg1)
                        Log.i(
                            "MY_BLUETOOTH",
                            "read: " + readMessage + "; nBytes: " + msg.arg1
                        )
                        val aux2 = "Recebido: $readMessage"
                        textviewBtRead.text = aux2
                    }
                    MESSAGE_WHAT_TOAST -> show(
                        this@ExecutarTeste1Activity,
                        msg.data.getString(BUNDLE_KEY_TOAST) ?: DEFAULT_STRING_VALUE,
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }

        configuraToolbar()

    }

    override fun onStart() {
        super.onStart()

        if (!isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        checkBtState() // verifica se o bluetooth existe, caso exista e esteja desligado, pede para ligar
    }

    override fun onStop() {
        super.onStop()
        if (threadConectada != null)
            (threadConectada as ConnectedThread).cancel()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (threadParaConectar != null)
            (threadParaConectar as ConnectThread).cancel()
    }

    private fun checkBtState() {
        when {
            bluetoothAdapter == null -> {
                show(this, getString(R.string.dispositivo_nao_suporta_bluetooth), Toast.LENGTH_LONG)
                finish()
            }
            bluetoothAdapter.isEnabled -> { // se esta ativado, entao verifica a conexao
                checkBtConnection()
            }
            else -> { // pede permissao para ligar o bt
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, ACTIVITY_REQUEST_LIGAR_BT)
            }
        }
    }

    private fun checkBtConnection() {
        deviceMacAddress = sharedPreferences.getString(SP_KEY_DEVICE_MAC_ADDRESS, DEFAULT_STRING_VALUE) ?: DEFAULT_STRING_VALUE
        Log.i("MY_BLUETOOTH", "check_bt: $deviceMacAddress")
        if (deviceMacAddress == DEFAULT_STRING_VALUE) { // inicia a activity para conectar em um dispositovo
            startActivityForResult(
                Intent(this, ListarDispositivosBTActivity::class.java),
                ACTIVITY_REQUEST_CONECTAR_DISPOSITIVO_BT
            )
        } else {
            if (bluetoothSocket != null) {
                if ((bluetoothSocket as BluetoothSocket).isConnected) { // ja esta conectado
                    Log.i("MY_BLUETOOTH", "check_bt: jah conectado")
                    return
                }
            }
            bluetoothDevice = bluetoothAdapter?.getRemoteDevice(deviceMacAddress)
            threadParaConectar =
                ConnectThread(bluetoothDevice)
            progressBar.visibility = View.VISIBLE
            (threadParaConectar as ConnectThread).start()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_REQUEST_LIGAR_BT) {
            if (resultCode == Activity.RESULT_OK) { // bluetooth ativado, agora verifica a conexao
                checkBtConnection()
            } else { // bluetooth nao foi ativado
                show(this, getString(R.string.falha_ao_ativar_o_bluetooth), Toast.LENGTH_LONG)
                finish()
            }
        }
        if (requestCode == ACTIVITY_REQUEST_CONECTAR_DISPOSITIVO_BT) {
            if (resultCode == Activity.RESULT_OK) {
                deviceMacAddress = data?.getStringExtra(INTENT_KEY_DEVICE_MAC_ADDRESS) ?: DEFAULT_STRING_VALUE

                // salva para usar futuramente (evita ficar selecionando o dispositivo toda hora)
                val editor = sharedPreferences.edit()
                editor.putString(SP_KEY_DEVICE_MAC_ADDRESS, deviceMacAddress)
                editor.apply()
                Log.i("MY_BLUETOOTH", "act result: $deviceMacAddress")
            } else {
                show(this, getString(R.string.falha_ao_selecionar_um_dispositivo), Toast.LENGTH_LONG)
                finish()
            }
        }
    }

    private fun configuraToolbar() { // adiciona a barra de tarefas na tela
        val myToolbar = findViewById<Toolbar>(R.id.my_toolbar_main)
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

    fun btnLigarTeste(view: View?) {
        val tempoAux = editTextTempoParaExecucao.text.toString()
        if (tempoAux.isEmpty()) {
            show(this, getString(R.string.digite_a_duracao_do_teste), Toast.LENGTH_SHORT)
            return
        }
        if (bluetoothSocket == null) {
            show(this, getString(R.string.dispositivo_nao_conectado), Toast.LENGTH_SHORT)
            return
        }
        if (!((bluetoothSocket as BluetoothSocket).isConnected)) { // nao esta conectado
            show(this, getString(R.string.dispositivo_nao_conectado), Toast.LENGTH_SHORT)
            return
        }
        if (tempoAux.toInt() < 1) {
            show(this, getString(R.string.digite_um_valor_maior_que_0), Toast.LENGTH_SHORT)
            return
        }
        if (estado == "0") { // ligar
            estado = alterarEstado(BT_COMMAND_LIGAR_LED)
            tempoDoTeste = tempoAux.toInt()
            Log.i("MY_TESTE", "tempo do teste: $tempoDoTeste")
            (findViewById<View>(R.id.btn_ligar) as Button).text = getString(R.string.teste_em_andamento)
            try {
                val imm =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            } catch (e: Exception) { // TODO: handle exception
            }
            (findViewById<View>(R.id.teste_status) as TextView).text = getString(R.string.teste_em_andamento)
            startTime = System.currentTimeMillis()
            timerHandler.postDelayed(timerRunnable, 0)
            val handler = Handler()
            handler.postDelayed({ finalizarTeste() }, tempoAux.toInt() * 1000.toLong())
            val handler2 = Handler()
            handler2.postDelayed({ desligaBuzzy() }, tempoAux.toInt() * 1000 + 500.toLong())
        }
    }

    private fun desligaBuzzy() {
        estado = alterarEstado(BT_COMMAND_DESLIGAR_BUZZER)
        (findViewById<View>(R.id.btn_ligar) as Button).text = getString(R.string.executar)
        estado = alterarEstado(BT_COMMAND_DESLIGAR_LED)
        (findViewById<View>(R.id.teste_status) as TextView).text = getString(R.string.teste_finalizado)
    }

    private fun finalizarTeste() {
        estado = alterarEstado(BT_COMMAND_LIGAR_BUZZER)
        (findViewById<View>(R.id.teste_status) as TextView).text = getString(R.string.finalizando)
        timerHandler.removeCallbacks(timerRunnable)
    }

    private fun alterarEstado(novoEstado: String): String {
        if (threadConectada == null) {
            show(this, getString(R.string.dispositivo_nao_conectado), Toast.LENGTH_SHORT)
        } else {
            (threadConectada as ConnectedThread).write(novoEstado.toByteArray())
        }
        return novoEstado
    }

    private inner class ConnectThread(device: BluetoothDevice?) : Thread() {
        //private final BluetoothSocket mmSocket;
        private val mmDevice: BluetoothDevice?

        init {
            Log.i("MY_BLUETOOTH", "new thread connect")
            // Use a temporary object that is later assigned to mmSocket
// because mmSocket is final.
            var tmp: BluetoothSocket? = null
            mmDevice = device
            try { // Get a BluetoothSocket to connect with the given BluetoothDevice.
// MY_UUID is the app's UUID string, also used in the server code.
                tmp = device!!.createRfcommSocketToServiceRecord(BT_MODULE_UUID)
            } catch (e: Exception) {
                Log.e("MY_BLUETOOTH", "Socket's create() method failed", e)
            }
            bluetoothSocket = tmp
        }

        override fun run() {
            Log.i("MY_BLUETOOTH", "run thread connect")
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()
            try { // Connect to the remote device through the socket. This call blocks
// until it succeeds or throws an exception.
                runOnUiThread {
                    (findViewById<View>(R.id.status_bluetooth_debug) as TextView).text =
                        getString(R.string.status_conectando)
                }
                bluetoothSocket!!.connect()
            } catch (connectException: Exception) {
                Log.e("MY_BLUETOOTH", "Unable to connect", connectException)
                // Unable to connect; close the socket and return.
                try {
                    bluetoothSocket!!.close()
                } catch (closeException: Exception) {
                    Log.e(
                        "MY_BLUETOOTH",
                        "Could not close the client socket",
                        closeException
                    )
                }
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    show(
                        this@ExecutarTeste1Activity,
                        getString(R.string.falha_ao_conectar_no_dispositivo),
                        Toast.LENGTH_LONG
                    )
                    sharedPreferences.edit().remove(SP_KEY_DEVICE_MAC_ADDRESS).apply()
                    //startActivityForResult(Intent( this@ExecutarTeste1,ListaDispositivosBT::class.java), CONECTAR_DISPOSITIVO_REQUEST)
                    (findViewById<View>(R.id.status_bluetooth_debug) as TextView).text =
                        getString(R.string.status_desconectado)
                    finish()
                }
                return
            }
            runOnUiThread {
                progressBar.visibility = View.GONE
                (findViewById<View>(R.id.status_bluetooth_debug) as TextView).text =
                    getString(R.string.status_conectado)
            }
            // The connection attempt succeeded. Perform work associated with
// the connection in a separate thread.
            threadConectada = ConnectedThread()
            (threadConectada as ConnectedThread).start()
            interrupt()
        }

        fun cancel() {
            Log.i("MY_BLUETOOTH", "cancel thread connect")
            try {
                bluetoothSocket!!.close()
            } catch (e: Exception) {
                Log.e("MY_BLUETOOTH", "Could not close the connect socket", e)
            }
            interrupt()
        }
    }

    private inner class ConnectedThread : Thread() {
        //private final BluetoothSocket mmSocket;
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        private lateinit var mmBuffer // mmBuffer store for the stream
                : ByteArray

        override fun run() {
            Log.i("MY_BLUETOOTH", "run thread connected")
            mmBuffer = ByteArray(1024)
            var numBytes: Int // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs.
            while (true) {

                try { // Read from the InputStream.
                    //Log.i("MY_BLUETOOTH", "while thread connected entrou")
                    numBytes = mmInStream!!.read(mmBuffer)
                    //Log.i("MY_BLUETOOTH", "while thread connected: $numBytes")
                    // Send the obtained bytes to the UI activity.
                    val readMsg: Message = bluetoothIo!!.obtainMessage(
                        MESSAGE_WHAT_READ,
                        numBytes,
                        -1,
                        mmBuffer
                    )
                    //Message readMsg = bluetooth_io.obtainMessage(MESSAGE_READ, 1, -1, numBytes);
                    readMsg.sendToTarget()
                } catch (e: Exception) {
                    Log.i("MY_BLUETOOTH", "Input stream was disconnected $e")
                    try {
                        bluetoothSocket!!.close()
                    } catch (e2: Exception) {
                        Log.e("MY_BLUETOOTH", "Could not close the connect socket", e2)
                    }
                    runOnUiThread {
                        (findViewById<View>(R.id.status_bluetooth_debug) as TextView).text =
                            getString(R.string.status_desconectado)
                        show(
                            this@ExecutarTeste1Activity,
                            getString(R.string.fluxo_entrada_interrompido),
                            Toast.LENGTH_LONG
                        )
                    }
                    break
                }
            }
            interrupt()
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream!!.write(bytes)
                // Share the sent message with the UI activity.
                val writtenMsg: Message =
                    bluetoothIo!!.obtainMessage(MESSAGE_WHAT_WRITE, bytes)
                writtenMsg.sendToTarget()
            } catch (e: Exception) {
                try {
                    Log.e("MY_BLUETOOTH", "Error occurred when sending data", e)
                    // Send a failure message back to the activity.
                    val writeErrorMsg: Message =
                        bluetoothIo!!.obtainMessage(MESSAGE_WHAT_TOAST)
                    val bundle = Bundle()
                    bundle.putString(
                        BUNDLE_KEY_TOAST,
                        getString(R.string.falha_ao_enviar_para_o_outro_dispositivo)
                    )
                    writeErrorMsg.data = bundle
                    bluetoothIo!!.sendMessage(writeErrorMsg)
                } catch (e: Exception) {
                }
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            Log.i("MY_BLUETOOTH", "cancel thread connected")
            try {
                bluetoothSocket!!.close()
            } catch (e: Exception) {
                Log.e("MY_BLUETOOTH", "Could not close the connect socket", e)
            }
            interrupt()
        }

        init { //mmSocket = socket;
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            // Get the input and output streams; using temp objects because
// member streams are final.
            try {
                tmpIn = bluetoothSocket?.inputStream
            } catch (e: Exception) {
                Log.e("MY_BLUETOOTH", "Error occurred when creating input stream", e)
            }
            try {
                tmpOut = bluetoothSocket?.outputStream
            } catch (e: Exception) {
                Log.e("MY_BLUETOOTH", "Error occurred when creating output stream", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            Log.i("MY_BLUETOOTH", "new thread connected")
        }
    }

    fun btn_desconectar(v: View) {
        val editor = sharedPreferences.edit()
        editor.remove(SP_KEY_DEVICE_MAC_ADDRESS)
        editor.apply()

        if (threadConectada != null)
            (threadConectada as ConnectedThread).cancel()

        finish()
    }

}
