package br.com.nutrisolver.activitys

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import br.com.nutrisolver.R
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

    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var textView_bt_read: TextView
    lateinit var textView_bt_send: TextView
    var startTime: Long = 0
    var timerHandler = Handler()
    private var device_mac_address: String = "-1"
    private var bt_socket: BluetoothSocket? = null
    private var bt_device: BluetoothDevice? = null
    private lateinit var thread_para_conectar: ExecutarTeste1.ConnectThread
    private lateinit var thread_conectada: ExecutarTeste1.ConnectedThread
    private var bluetooth_io: Handler? = null
    private lateinit var progressBar: ProgressBar
    private var estado = "0"
    private lateinit var tempo_para_execucao: EditText
    private var TEMPO_DO_TESTE = 10
    private lateinit var sharedpreferences: SharedPreferences

    var timerRunnable = object : Runnable{
        override fun run() {
            var millis : Long = System.currentTimeMillis()
            var seconds = (millis / 1000).toInt()
            var minutes = seconds / 60
            seconds %= 60

            findViewById<TextView>(R.id.teste_timer).text = Integer.toString(TEMPO_DO_TESTE - seconds - 1)

            timerHandler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_executar_teste1)


    }
}
