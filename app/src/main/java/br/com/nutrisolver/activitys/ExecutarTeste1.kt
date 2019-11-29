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
import br.com.nutrisolver.tools.ToastUtil.show
import br.com.nutrisolver.tools.UserUtil.isLogged
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class ExecutarTeste1 : AppCompatActivity() {
    val TOAST = "toast"
    private val MESSAGE_READ = 0
    private val MESSAGE_WRITE = 1
    private val MESSAGE_TOAST = 2
    private val BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val DESLIGAR_LED = "0"
    private val LIGAR_LED = "1"
    private val LIGAR_BUZZER = "2"
    private val DESLIGAR_BUZZER = "3"
    private val CONECTAR_DISPOSITIVO_REQUEST = 1001
    private val REQUEST_ENABLE_BT = 1

    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    lateinit var textView_bt_read: TextView
    lateinit var textView_bt_send: TextView
    var startTime: Long = 0
    var timerHandler = Handler()
    private var device_mac_address: String = "-1"
    private var bt_socket: BluetoothSocket? = null
    private var bt_device: BluetoothDevice? = null
    private var thread_para_conectar: ConnectThread? = null
    private var thread_conectada: ConnectedThread? = null
    private var bluetooth_io: Handler? = null
    private lateinit var progressBar: ProgressBar
    private var estado = "0"
    private lateinit var tempo_para_execucao: EditText
    private var TEMPO_DO_TESTE = 10
    private lateinit var sharedpreferences: SharedPreferences

    var timerRunnable = object : Runnable {
        override fun run() {
            var millis: Long = System.currentTimeMillis() - startTime
            var seconds = (millis / 1000).toInt()
            var minutes = seconds / 60
            seconds %= 60

            Log.i("MY_TESTE", ": $TEMPO_DO_TESTE")

            findViewById<TextView>(R.id.teste_timer).text =
                (TEMPO_DO_TESTE - seconds - 1).toString()

            timerHandler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_executar_teste1)

        progressBar = findViewById(R.id.progress_bar)
        tempo_para_execucao = findViewById(R.id.tempo_para_execucao)
        textView_bt_read = findViewById(R.id.leitura_bluetooth_debug)
        textView_bt_send = findViewById(R.id.envio_bluetooth_debug)
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE)

        //bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        bluetooth_io = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_WRITE -> {
                        val writeBuf = msg.obj as ByteArray
                        val writeMessage = String(writeBuf, 0, writeBuf.size)
                        Log.i(
                            "MY_BLUETOOTH",
                            "write: " + writeMessage + "; nBytes: " + writeBuf.size
                        )
                        val aux1 = "Enviado: $writeMessage"
                        textView_bt_send.text = aux1
                    }
                    MESSAGE_READ -> {
                        val readBuf = msg.obj as ByteArray
                        // construct a string from the valid bytes in the buffer
                        val readMessage = String(readBuf, 0, msg.arg1)
                        Log.i(
                            "MY_BLUETOOTH",
                            "read: " + readMessage + "; nBytes: " + msg.arg1
                        )
                        val aux2 = "Recebido: $readMessage"
                        textView_bt_read.text = aux2
                    }
                    MESSAGE_TOAST -> show(
                        this@ExecutarTeste1,
                        msg.data.getString(TOAST) ?: "-1",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }

        configura_toolbar()

    }

    override fun onStart() {
        super.onStart()

        if (!isLogged()) {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
        check_bt_state() // verifica se o bluetooth existe, caso exista e esteja desligado, pede para ligar
    }

    override fun onStop() {
        super.onStop()
        if (thread_conectada != null)
            (thread_conectada as ConnectedThread).cancel()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (thread_para_conectar != null)
            (thread_para_conectar as ConnectThread).cancel()
    }

    private fun check_bt_state() {
        when {
            bluetoothAdapter == null -> {
                show(this, "Device does not support bluetooth", Toast.LENGTH_LONG)
                finish()
            }
            bluetoothAdapter.isEnabled -> { // se esta ativado, entao verifica a conexao
                check_bt_connection()
            }
            else -> { // pede permissao para ligar o bt
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    private fun check_bt_connection() {
        device_mac_address = sharedpreferences.getString("device_mac_address", "-1") ?: "-1"
        Log.i("MY_BLUETOOTH", "check_bt: $device_mac_address")
        if (device_mac_address.equals("-1")) { // inicia a activity para conectar em um dispositovo
            startActivityForResult(
                Intent(this, ListaDispositivosBT::class.java),
                CONECTAR_DISPOSITIVO_REQUEST
            )
        } else {
            if (bt_socket != null) {
                if ((bt_socket as BluetoothSocket).isConnected) { // ja esta conectado
                    Log.i("MY_BLUETOOTH", "check_bt: jah conectado")
                    return
                }
            }
            bt_device = bluetoothAdapter?.getRemoteDevice(device_mac_address)
            thread_para_conectar =
                ConnectThread(bt_device)
            progressBar.visibility = View.VISIBLE
            (thread_para_conectar as ConnectThread).start()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) { // bluetooth ativado, agora verifica a conexao
                check_bt_connection()
            } else { // bluetooth nao foi ativado
                show(this, "Falha ao ativar o bluetooth", Toast.LENGTH_LONG)
                finish()
            }
        }
        if (requestCode == CONECTAR_DISPOSITIVO_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                device_mac_address = data?.getStringExtra("device_mac_address") ?: "-1"
                // salva para usar futuramente (evita ficar selecionando o dispositivo toda hora)
                val editor = sharedpreferences.edit()
                editor.putString("device_mac_address", device_mac_address)
                editor.apply()
                Log.i("MY_BLUETOOTH", "act result: $device_mac_address")
            } else {
                show(this, "Falha ao selecionar um dispositivo", Toast.LENGTH_LONG)
                finish()
            }
        }
    }

    private fun configura_toolbar() { // adiciona a barra de tarefas na tela
        val my_toolbar = findViewById<Toolbar>(R.id.my_toolbar_main)
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

    fun btn_ligar_teste(view: View?) {
        val tempo_aux = tempo_para_execucao.text.toString()
        if (tempo_aux.isEmpty()) {
            show(this, "Digite a duração do teste!", Toast.LENGTH_SHORT)
            return
        }
        if (bt_socket == null) {
            show(this, "Dispositivo não conectado", Toast.LENGTH_SHORT)
            return
        }
        if (!((bt_socket as BluetoothSocket).isConnected)) { // nao esta conectado
            show(this, "Dispositivo não conectado", Toast.LENGTH_SHORT)
            return
        }
        if (tempo_aux.toInt() < 1) {
            show(this, "Digite um valor maior que 0", Toast.LENGTH_SHORT)
            return
        }
        if (estado == "0") { // ligar
            estado = alterarEstado(LIGAR_LED)
            TEMPO_DO_TESTE = tempo_aux.toInt()
            Log.i("MY_TESTE", "tempo do teste: $TEMPO_DO_TESTE")
            (findViewById<View>(R.id.btn_ligar) as Button).text = "Teste em andamento.."
            try {
                val imm =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            } catch (e: Exception) { // TODO: handle exception
            }
            (findViewById<View>(R.id.teste_status) as TextView).text = "Teste em andamento.."
            startTime = System.currentTimeMillis()
            timerHandler.postDelayed(timerRunnable, 0)
            val handler = Handler()
            handler.postDelayed({ finalizarTeste() }, tempo_aux.toInt() * 1000.toLong())
            val handler2 = Handler()
            handler2.postDelayed({ desligaBuzzy() }, tempo_aux.toInt() * 1000 + 500.toLong())
        }
    }

    private fun desligaBuzzy() {
        estado = alterarEstado(DESLIGAR_BUZZER)
        (findViewById<View>(R.id.btn_ligar) as Button).text = "Executar"
        estado = alterarEstado(DESLIGAR_LED)
        (findViewById<View>(R.id.teste_status) as TextView).text = "Teste finalizado!"
    }

    private fun finalizarTeste() {
        estado = alterarEstado(LIGAR_BUZZER)
        (findViewById<View>(R.id.teste_status) as TextView).text = "Finalizando.."
        timerHandler.removeCallbacks(timerRunnable)
    }

    private fun alterarEstado(novoEstado: String): String {
        if (thread_conectada == null) {
            show(this, "Dispositivo não conectado", Toast.LENGTH_SHORT)
        } else {
            (thread_conectada as ConnectedThread).write(novoEstado.toByteArray())
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
                tmp = device!!.createRfcommSocketToServiceRecord(BTMODULEUUID)
            } catch (e: Exception) {
                Log.e("MY_BLUETOOTH", "Socket's create() method failed", e)
            }
            bt_socket = tmp
        }

        override fun run() {
            Log.i("MY_BLUETOOTH", "run thread connect")
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()
            try { // Connect to the remote device through the socket. This call blocks
// until it succeeds or throws an exception.
                this@ExecutarTeste1.runOnUiThread(Runnable {
                    (findViewById<View>(R.id.status_bluetooth_debug) as TextView).text =
                        "Status: conectando.."
                })
                bt_socket!!.connect()
            } catch (connectException: Exception) {
                Log.e("MY_BLUETOOTH", "Unable to connect", connectException)
                // Unable to connect; close the socket and return.
                try {
                    bt_socket!!.close()
                } catch (closeException: Exception) {
                    Log.e(
                        "MY_BLUETOOTH",
                        "Could not close the client socket",
                        closeException
                    )
                }
                this@ExecutarTeste1.runOnUiThread(Runnable {
                    progressBar.visibility = View.GONE
                    show(
                        this@ExecutarTeste1,
                        "Falha ao conectar no dispositivo",
                        Toast.LENGTH_LONG
                    )
                    sharedpreferences.edit().remove("device_mac_address").apply()
                    //startActivityForResult(Intent( this@ExecutarTeste1,ListaDispositivosBT::class.java), CONECTAR_DISPOSITIVO_REQUEST)
                    (findViewById<View>(R.id.status_bluetooth_debug) as TextView).text =
                        "Status: desconectado"
                    finish()
                })
                return
            }
            this@ExecutarTeste1.runOnUiThread(Runnable {
                progressBar.visibility = View.GONE
                (findViewById<View>(R.id.status_bluetooth_debug) as TextView).text =
                    "Status: conectado"
            })
            // The connection attempt succeeded. Perform work associated with
// the connection in a separate thread.
            thread_conectada = ConnectedThread()
            (thread_conectada as ConnectedThread).start()
            interrupt()
        }

        fun cancel() {
            Log.i("MY_BLUETOOTH", "cancel thread connect")
            try {
                bt_socket!!.close()
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
                    val readMsg: Message = bluetooth_io!!.obtainMessage(
                        MESSAGE_READ,
                        numBytes,
                        -1,
                        mmBuffer
                    )
                    //Message readMsg = bluetooth_io.obtainMessage(MESSAGE_READ, 1, -1, numBytes);
                    readMsg.sendToTarget()
                } catch (e: Exception) {
                    Log.i("MY_BLUETOOTH", "Input stream was disconnected $e")
                    try {
                        bt_socket!!.close()
                    } catch (e2: Exception) {
                        Log.e("MY_BLUETOOTH", "Could not close the connect socket", e2)
                    }
                    this@ExecutarTeste1.runOnUiThread(Runnable {
                        (findViewById<View>(R.id.status_bluetooth_debug) as TextView).text =
                            "Status: desconectado"
                        show(
                            this@ExecutarTeste1,
                            "Input stream desconectado",
                            Toast.LENGTH_LONG
                        )
                    })
                    break
                }
            }
            interrupt()
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray?) {
            try {
                mmOutStream!!.write(bytes)
                // Share the sent message with the UI activity.
                val writtenMsg: Message =
                    bluetooth_io!!.obtainMessage(MESSAGE_WRITE, bytes)
                writtenMsg.sendToTarget()
            } catch (e: Exception) {
                try {
                    Log.e("MY_BLUETOOTH", "Error occurred when sending data", e)
                    // Send a failure message back to the activity.
                    val writeErrorMsg: Message =
                        bluetooth_io!!.obtainMessage(MESSAGE_TOAST)
                    val bundle = Bundle()
                    bundle.putString(
                        "toast",
                        "Couldn't send data to the other device"
                    )
                    writeErrorMsg.data = bundle
                    bluetooth_io!!.sendMessage(writeErrorMsg)
                } catch (e: Exception) {
                }
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            Log.i("MY_BLUETOOTH", "cancel thread connected")
            try {
                bt_socket!!.close()
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
                tmpIn = bt_socket?.inputStream
            } catch (e: Exception) {
                Log.e("MY_BLUETOOTH", "Error occurred when creating input stream", e)
            }
            try {
                tmpOut = bt_socket?.outputStream
            } catch (e: Exception) {
                Log.e("MY_BLUETOOTH", "Error occurred when creating output stream", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            Log.i("MY_BLUETOOTH", "new thread connected")
        }
    }

    fun btn_desconectar(v: View) {
        val editor = sharedpreferences.edit()
        editor.remove("device_mac_address")
        editor.apply()

        if (thread_conectada != null)
            (thread_conectada as ConnectedThread).cancel()

        finish()
    }

}
