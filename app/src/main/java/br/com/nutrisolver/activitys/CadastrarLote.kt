package br.com.nutrisolver.activitys

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.com.nutrisolver.R
import br.com.nutrisolver.objects.Lote
import br.com.nutrisolver.tools.DataBaseUtil
import br.com.nutrisolver.tools.ToastUtil.show
import br.com.nutrisolver.tools.UserUtil.getCurrentUser
import br.com.nutrisolver.tools.UserUtil.isLogged
import java.util.*

class CadastrarLote : AppCompatActivity() {
    private lateinit var sharedpreferences: SharedPreferences
    private lateinit var input_nome_lote: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var lote: Lote
    private var fazenda_corrente_id: String = "-1"
    private var fazenda_corrente_nome: String = "-1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_lote)

        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1") ?: "-1"
        fazenda_corrente_nome = sharedpreferences.getString("fazenda_corrente_nome", "-1") ?: "-1"
        input_nome_lote = findViewById(R.id.cadastrar_nome_do_lote)
        progressBar = findViewById(R.id.progress_bar)
        configura_toolbar()
    }

    private fun configura_toolbar() { // adiciona a barra de tarefas na tela
        val my_toolbar =
            findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(my_toolbar)
        // adiciona a seta de voltar na barra de tarefas
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Fazenda: $fazenda_corrente_nome"
    }

    override fun onStart() {
        super.onStart()
        if (!isLogged()) {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
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

    fun cadastrar_lote(view: View?) {
        if (!validaDados()) {
            return
        }

        progressBar.visibility = View.VISIBLE
        val nome_lote = input_nome_lote.text.toString()

        lote = Lote()
        lote.nome = nome_lote
        lote.fazenda_id = fazenda_corrente_id
        lote.dono_uid = getCurrentUser()?.uid ?: "-1"

        DataBaseUtil.insertDocument("lotes", lote.id, lote)

        // envia o lote para o fragment
        Principal.sendData("LotesFragment", "adiciona_lote", lote)
        val handler = Handler()
        handler.postDelayed({ finaliza_cadastro() }, 800)
    }

    private fun finaliza_cadastro() {
        show(applicationContext, "Lote cadastrado com sucesso!", Toast.LENGTH_SHORT)
        progressBar.visibility = View.GONE
        val it = Intent()
        it.putExtra("lote_cadastrado", lote)
        setResult(1, it)
        finish()
    }

    private fun validaDados(): Boolean {
        var valido = true
        val nome_lote = input_nome_lote.text.toString()
        if (TextUtils.isEmpty(nome_lote)) {
            input_nome_lote.error = "Campo necessário."
            valido = false
        } else {
            input_nome_lote.error = null
        }
        return valido
    }
}
