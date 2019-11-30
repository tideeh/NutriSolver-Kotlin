package br.com.nutrisolver.activitys

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.com.nutrisolver.R
import br.com.nutrisolver.models.Fazenda
import br.com.nutrisolver.adapters.AdapterFazenda
import br.com.nutrisolver.utils.*

class SelecionarFazendaActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var listviewFazendas: ListView
    private lateinit var adapterFazenda: AdapterFazenda
    private lateinit var progressBar: ProgressBar
    private lateinit var myToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selecionar_fazenda)

        sharedPreferences = getSharedPreferences(SP_NOME, Context.MODE_PRIVATE)

        progressBar = findViewById(R.id.progress_bar)

        configuraListview()
        atualizaListaDeFazendas()
        configuraToolbar()

    }

    private fun configuraListview() {
        listviewFazendas = findViewById(R.id.lista_fazendas)
        adapterFazenda = AdapterFazenda(this)
        listviewFazendas.adapter = adapterFazenda
        listviewFazendas.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val editor = sharedPreferences.edit()
                editor.putString(SP_KEY_FAZENDA_CORRENTE_ID, adapterFazenda.getItemIdString(position))
                editor.putString(
                    SP_KEY_FAZENDA_CORRENTE_NOME,
                    (adapterFazenda.getItem(position) as Fazenda).nome
                )
                editor.apply()

                val it = Intent(view.context, PrincipalActivity::class.java)
                startActivity(it)
                finish()
            }
    }

    private fun atualizaListaDeFazendas() {
        progressBar.visibility = View.VISIBLE

        DataBaseUtil.getDocumentsWhereEqualTo(
            DB_COLLECTION_FAZENDAS,
            arrayOf(Fazenda::donoUid.name, Fazenda::ativo.name),
            arrayOf(UserUtil.getCurrentUser()?.uid ?: DEFAULT_STRING_VALUE, true)
        )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("MY_FIRESTORE", "successful")
                    adapterFazenda.clear()
                    val documents = task.result
                    if (documents != null) {
                        Log.i("MY_FIRESTORE", "!= null")
                        for (document in documents) {
                            val fazenda = document.toObject(Fazenda::class.java)
                            Log.i("MY_FIRESTORE", "faz nome: ${fazenda.nome}")
                            adapterFazenda.addItem(fazenda)
                        }
                    }
                    progressBar.visibility = View.GONE
                } else {
                    Log.i("MY_FIRESTORE", "Error getting documents: " + task.exception)
                    progressBar.visibility = View.GONE
                }
            }
    }

    private fun configuraToolbar() {
        // adiciona a barra de tarefas na tela
        myToolbar = findViewById(R.id.my_toolbar_main)
        setSupportActionBar(myToolbar)
    }

    fun cadastrarFazenda(view: View) {
        startActivityForResult(Intent(this, CadastrarFazendaActivity::class.java), ACTIVITY_REQUEST_CADASTRAR_FAZENDA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ACTIVITY_REQUEST_CADASTRAR_FAZENDA && resultCode == Activity.RESULT_OK) {
            startActivity(Intent(this, PrincipalActivity::class.java))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_itens, menu)

        val mSearch = menu.findItem(R.id.mi_search)
        mSearch.isVisible = false

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.miDeslogar -> {
                logout()
                return true
            }

            R.id.mi_refresh -> return true

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        UserUtil.logOut()

        val editor = sharedPreferences.edit()
        editor.remove(SP_KEY_FAZENDA_CORRENTE_ID)
        editor.remove(SP_KEY_FAZENDA_CORRENTE_NOME)
        editor.apply()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()

        if (!UserUtil.isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
