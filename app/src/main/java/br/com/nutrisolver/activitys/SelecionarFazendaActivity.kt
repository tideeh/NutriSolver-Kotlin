package br.com.nutrisolver.activitys

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
import br.com.nutrisolver.utils.DataBaseUtil
import br.com.nutrisolver.utils.UserUtil

class SelecionarFazendaActivity : AppCompatActivity() {
    private lateinit var sharedpreferences: SharedPreferences

    private lateinit var listView_Fazendas: ListView
    private lateinit var adapterFazenda: AdapterFazenda
    private lateinit var progressBar: ProgressBar
    private lateinit var my_toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selecionar_fazenda)

        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE)

        progressBar = findViewById(R.id.progress_bar)

        configura_listView()
        atualiza_lista_de_fazendas()
        configura_toolbar()

    }

    private fun configura_listView() {
        listView_Fazendas = findViewById(R.id.lista_fazendas)
        adapterFazenda = AdapterFazenda(this)
        listView_Fazendas.adapter = adapterFazenda
        listView_Fazendas.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val editor = sharedpreferences.edit()
                editor.putString("fazenda_corrente_id", adapterFazenda.getItemIdString(position))
                editor.putString(
                    "fazenda_corrente_nome",
                    (adapterFazenda.getItem(position) as Fazenda).nome
                )
                editor.apply()

                val it = Intent(view.context, PrincipalActivity::class.java)
                startActivity(it)
                finish()
            }
    }

    private fun atualiza_lista_de_fazendas() {
        progressBar.visibility = View.VISIBLE

        DataBaseUtil.getDocumentsWhereEqualTo(
            "fazendas",
            "dono_uid",
            UserUtil.getCurrentUser()?.uid ?: "-1"
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

    private fun configura_toolbar() {
        // adiciona a barra de tarefas na tela
        my_toolbar = findViewById(R.id.my_toolbar_main)
        setSupportActionBar(my_toolbar)
    }

    fun cadastrar_fazenda(view: View) {
        startActivityForResult(Intent(this, CadastrarFazendaActivity::class.java), 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == 1) {
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

        val editor = sharedpreferences.edit()
        editor.remove("fazenda_corrente_id")
        editor.remove("fazenda_corrente_nome")
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
