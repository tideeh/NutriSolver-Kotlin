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
import br.com.nutrisolver.models.Lote
import br.com.nutrisolver.utils.*
import br.com.nutrisolver.utils.ToastUtil.show
import br.com.nutrisolver.utils.UserUtil.getCurrentUser
import br.com.nutrisolver.utils.UserUtil.isLogged

class CadastrarLoteActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editTextNomeLote: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var lote: Lote
    private var fazendaCorrenteId: String = DEFAULT_STRING_VALUE
    private var fazendaCorrenteNome: String = DEFAULT_STRING_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_lote)

        sharedPreferences = getSharedPreferences(SP_NOME, Context.MODE_PRIVATE)
        fazendaCorrenteId = sharedPreferences.getString(SP_KEY_FAZENDA_CORRENTE_ID, DEFAULT_STRING_VALUE) ?: DEFAULT_STRING_VALUE
        fazendaCorrenteNome = sharedPreferences.getString(SP_KEY_FAZENDA_CORRENTE_NOME, DEFAULT_STRING_VALUE) ?: DEFAULT_STRING_VALUE
        editTextNomeLote = findViewById(R.id.cadastrar_nome_do_lote)
        progressBar = findViewById(R.id.progress_bar)
        configuraToolbar()
    }

    private fun configuraToolbar() { // adiciona a barra de tarefas na tela
        val myToolbar =
            findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(myToolbar)
        // adiciona a seta de voltar na barra de tarefas
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.fazenda)+": $fazendaCorrenteNome"
    }

    override fun onStart() {
        super.onStart()

        if (!isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
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

    fun cadastrarLote(view: View?) {
        if (!validaDados()) {
            return
        }

        progressBar.visibility = View.VISIBLE
        val nomeLote = editTextNomeLote.text.toString()

        lote = Lote()
        lote.nome = nomeLote
        lote.fazenda_id = fazendaCorrenteId
        lote.dono_uid = getCurrentUser()?.uid ?: DEFAULT_STRING_VALUE

        DataBaseUtil.insertDocument(DB_COLLECTION_LOTES, lote.id, lote)

        // envia o lote para o fragment
        PrincipalActivity.sendData(SEND_DATA_FRAGMENT_LOTES, SEND_DATA_COMMAND_ADD_LOTE, lote)
        val handler = Handler()
        handler.postDelayed({ finalizaCadastro() }, 800)
    }

    private fun finalizaCadastro() {
        show(applicationContext, getString(R.string.lote_cadastrado_com_sucesso), Toast.LENGTH_SHORT)
        progressBar.visibility = View.GONE
        val it = Intent()
        it.putExtra(INTENT_KEY_LOTE_CADASTRADO, lote)
        setResult(1, it)
        finish()
    }

    private fun validaDados(): Boolean {
        var valido = true
        val nomeLote = editTextNomeLote.text.toString()
        if (TextUtils.isEmpty(nomeLote)) {
            editTextNomeLote.error = getString(R.string.campo_necessario)
            valido = false
        } else {
            editTextNomeLote.error = null
        }
        return valido
    }
}
