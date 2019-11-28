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
import br.com.nutrisolver.objects.Fazenda
import br.com.nutrisolver.tools.DataBaseUtil
import br.com.nutrisolver.tools.ToastUtil.show
import br.com.nutrisolver.tools.UserUtil.getCurrentUser
import br.com.nutrisolver.tools.UserUtil.isLogged
import java.text.SimpleDateFormat
import java.util.*

class CadastrarFazenda : AppCompatActivity() {
    //private FirebaseFirestore db;
    private lateinit var input_nome_fazenda: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedpreferences: SharedPreferences
    private lateinit var fazenda: Fazenda

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_fazenda)

        input_nome_fazenda = findViewById(R.id.cadastrar_nome_da_fazenda)
        progressBar = findViewById(R.id.progress_bar)
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE)

        configura_toolbar()
    }

    private fun configura_toolbar() { // adiciona a barra de tarefas na tela
        val my_toolbar = findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(my_toolbar)
        // adiciona a seta de voltar na barra de tarefas
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()
        if (!isLogged()) {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    fun cadastrar_fazenda(view: View?) {
        if (!validaDados()) {
            return
        }
        progressBar.visibility = View.VISIBLE
        val nome_fazenda = input_nome_fazenda.text.toString()
        fazenda = Fazenda()
        fazenda.nome = nome_fazenda
        fazenda.dono_uid = getCurrentUser()?.uid ?: "-1"

        DataBaseUtil.insertDocument("fazendas", fazenda.id, fazenda)

        val editor = sharedpreferences.edit()
        editor.putString("fazenda_corrente_id", fazenda.id)
        editor.putString("fazenda_corrente_nome", fazenda.nome)
        editor.apply()

        val handler = Handler()
        handler.postDelayed({ finaliza_cadastro() }, 800)
    }

    private fun finaliza_cadastro() {
        show(
            applicationContext,
            "Fazenda cadastrada com sucesso!",
            Toast.LENGTH_SHORT
        )
        progressBar.visibility = View.GONE
        val it = Intent()
        setResult(1, it)
        finish()
    }

    private fun validaDados(): Boolean {
        var valido = true
        val nome_fazenda = input_nome_fazenda.text.toString()
        if (TextUtils.isEmpty(nome_fazenda)) {
            input_nome_fazenda.error = "Campo necessário."
            valido = false
        } else {
            input_nome_fazenda.error = null
        }
        return valido
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
}
