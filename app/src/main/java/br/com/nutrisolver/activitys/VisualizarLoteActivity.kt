package br.com.nutrisolver.activitys

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.com.nutrisolver.R
import br.com.nutrisolver.models.Dieta
import br.com.nutrisolver.adapters.AdapterDieta
import br.com.nutrisolver.utils.*
import br.com.nutrisolver.utils.UserUtil.isLogged

class VisualizarLoteActivity : AppCompatActivity() {
    private var loteId: String = DEFAULT_STRING_VALUE
    private var loteNome: String = DEFAULT_STRING_VALUE
    private lateinit var progressBar: ProgressBar
    private lateinit var listviewDietas: ListView
    private lateinit var adapterDieta: AdapterDieta

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualiza_lote)

        progressBar = findViewById(R.id.progress_bar)

        val it = intent
        loteId = it.getStringExtra(INTENT_KEY_LOTE_SELECIONADO_ID) ?: DEFAULT_STRING_VALUE
        loteNome = it.getStringExtra(INTENT_KEY_LOTE_SELECIONADO_NOME) ?: DEFAULT_STRING_VALUE

        if (loteId == DEFAULT_STRING_VALUE || loteNome == DEFAULT_STRING_VALUE) {
            finish()
        }

        configura_listView()
        atualizaListaDieta()
        configuraToolbar()

        findViewById<View>(R.id.fab_cadastrar_dieta).setOnClickListener {
            val ite = Intent(this@VisualizarLoteActivity, CadastrarDietaActivity::class.java)
            ite.putExtra(INTENT_KEY_LOTE_SELECIONADO_ID, loteId)
            startActivityForResult(ite, ACTIVITY_REQUEST_CADASTRAR_DIETA)
        }
    }

    private fun atualizaListaDieta() {
        progressBar.visibility = View.VISIBLE
        DataBaseUtil.getDocumentsWhereEqualTo(
            DB_COLLECTION_DIETAS,
            arrayOf(Dieta::loteId.name, Dieta::ativo.name),
            arrayOf(loteId, true)
        )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    adapterDieta.clear()
                    val documents = task.result
                    if (documents != null) {
                        for (document in documents) {
                            adapterDieta.addItem(
                                document.toObject(
                                    Dieta::class.java
                                )
                            )
                        }
                    }
                } else {
                    Log.i(
                        "MY_FIRESTORE",
                        "atualiza_lista_dieta: " + task.exception
                    )
                }
                progressBar.visibility = View.GONE
            }
    }

    private fun configuraToolbar() { // adiciona a barra de tarefas na tela
        val myToolbar =
            findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(myToolbar)
        // adiciona a seta de voltar na barra de tarefas
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.lote)+": $loteNome"
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_REQUEST_CADASTRAR_DIETA && resultCode == Activity.RESULT_OK) { // foi cadastrada nova dieta, verifica se é desse lote, se for adiciona na lista
            val d = data?.getParcelableExtra<Parcelable>(INTENT_KEY_DIETA_CADASTRADA) as Dieta
            if (d.loteId == loteId) {
                adapterDieta.addItem(d)
            }
        }
    }

    private fun configura_listView() {
        listviewDietas =
            findViewById<View>(R.id.listView_dietas) as ListView
        adapterDieta = AdapterDieta(this)
        listviewDietas.adapter = adapterDieta
    }
}
