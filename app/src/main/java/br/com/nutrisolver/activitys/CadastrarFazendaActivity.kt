package br.com.nutrisolver.activitys

import android.app.Activity
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
import br.com.nutrisolver.models.Fazenda
import br.com.nutrisolver.utils.*
import br.com.nutrisolver.utils.ToastUtil.show
import br.com.nutrisolver.utils.UserUtil.getCurrentUser
import br.com.nutrisolver.utils.UserUtil.isLogged

class CadastrarFazendaActivity : AppCompatActivity() {
    //private FirebaseFirestore db;
    private lateinit var editTextNomeFazenda: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fazenda: Fazenda

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_fazenda)

        editTextNomeFazenda = findViewById(R.id.cadastrar_nome_da_fazenda)
        progressBar = findViewById(R.id.progress_bar)
        sharedPreferences = getSharedPreferences(SP_NOME, Context.MODE_PRIVATE)

        configuraToolbar()
    }

    private fun configuraToolbar() { // adiciona a barra de tarefas na tela
        val myToolbar = findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(myToolbar)
        // adiciona a seta de voltar na barra de tarefas
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()

        if (!isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    fun cadastrarFazenda(view: View?) {
        if (!validaDados()) {
            return
        }
        progressBar.visibility = View.VISIBLE
        val nomeFazenda = editTextNomeFazenda.text.toString()
        fazenda = Fazenda()
        fazenda.nome = nomeFazenda
        fazenda.donoUid = getCurrentUser()?.uid ?: DEFAULT_STRING_VALUE

        DataBaseUtil.insertDocument(DB_COLLECTION_FAZENDAS, fazenda.id, fazenda)

        val editor = sharedPreferences.edit()
        editor.putString(SP_KEY_FAZENDA_CORRENTE_ID, fazenda.id)
        editor.putString(SP_KEY_FAZENDA_CORRENTE_NOME, fazenda.nome)
        editor.apply()

        val handler = Handler()
        handler.postDelayed({ finalizaCadastro() }, 800)
    }

    private fun finalizaCadastro() {
        show(
            applicationContext,
            getString(R.string.fazenda_cadastrada_com_sucesso),
            Toast.LENGTH_SHORT
        )
        progressBar.visibility = View.GONE
        val it = Intent()
        setResult(Activity.RESULT_OK, it)
        finish()
    }

    private fun validaDados(): Boolean {
        var valido = true
        val nomeFazenda = editTextNomeFazenda.text.toString()
        if (TextUtils.isEmpty(nomeFazenda)) {
            editTextNomeFazenda.error = getString(R.string.campo_necessario)
            valido = false
        } else {
            editTextNomeFazenda.error = null
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
