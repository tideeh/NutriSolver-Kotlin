package br.com.nutrisolver.activitys

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
import br.com.nutrisolver.objects.Dieta
import br.com.nutrisolver.tools.AdapterDieta
import br.com.nutrisolver.tools.DataBaseUtil
import br.com.nutrisolver.tools.UserUtil.isLogged
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

class VisualizaLote : AppCompatActivity() {
    private var lote_id: String = "-1"
    private var lote_nome: String = "-1"
    private lateinit var progressBar: ProgressBar
    private lateinit var listView_dietas: ListView
    private lateinit var adapterDieta: AdapterDieta

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualiza_lote)

        progressBar = findViewById(R.id.progress_bar)

        val it = intent
        lote_id = it.getStringExtra("lote_id") ?: "-1"
        lote_nome = it.getStringExtra("lote_nome") ?: "-1"

        if (lote_id.equals("-1") || lote_nome.equals("-1")) {
            finish()
        }

        configura_listView()
        atualiza_lista_dieta()
        configura_toolbar()
        findViewById<View>(R.id.fab_cadastrar_dieta).setOnClickListener {
            val ite = Intent(this@VisualizaLote, CadastrarDieta::class.java)
            ite.putExtra("lote_selecionado_id", lote_id)
            startActivityForResult(ite, CADASTRAR_DIETA_REQUEST)
        }
    }

    private fun atualiza_lista_dieta() {
        progressBar.visibility = View.VISIBLE
        DataBaseUtil.getDocumentsWhereEqualTo(
            "dietas",
            arrayOf("lote_id", "ativo"),
            arrayOf(lote_id, true)
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

    private fun configura_toolbar() { // adiciona a barra de tarefas na tela
        val my_toolbar =
            findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(my_toolbar)
        // adiciona a seta de voltar na barra de tarefas
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Lote: $lote_nome"
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CADASTRAR_DIETA_REQUEST && resultCode == 1) { // foi cadastrada nova dieta, verifica se é desse lote, se for adiciona na lista
            val d = data?.getParcelableExtra<Parcelable>("dieta_cadastrada") as Dieta
            if (d.lote_id == lote_id) {
                adapterDieta.addItem(d)
            }
        }
    }

    private fun configura_listView() {
        listView_dietas =
            findViewById<View>(R.id.listView_dietas) as ListView
        adapterDieta = AdapterDieta(this)
        listView_dietas.adapter = adapterDieta
    }

    companion object {
        private const val CADASTRAR_DIETA_REQUEST = 1001
    }
}
