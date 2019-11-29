package br.com.nutrisolver.activitys

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.com.nutrisolver.R
import br.com.nutrisolver.objects.Dieta
import br.com.nutrisolver.objects.Ingrediente
import br.com.nutrisolver.objects.Lote
import br.com.nutrisolver.tools.AdapterIngredienteNome
import br.com.nutrisolver.tools.DataBaseUtil
import br.com.nutrisolver.tools.ToastUtil.show
import br.com.nutrisolver.tools.UserUtil
import br.com.nutrisolver.tools.UserUtil.getCurrentUser
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.QuerySnapshot

class CadastrarDieta : AppCompatActivity() {
    private val TIMEOUT_DB = 30 * 60 * 1000.toLong() // ms (MIN * 60 * 100)
    var ingredientes_nomes: ArrayList<String> = ArrayList()
    lateinit var dieta: Dieta
    private lateinit var sharedpreferences: SharedPreferences
    private val lote_id: String = "-1"
    private val lote_nome: String = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var listView_editar_ingredientes: ListView
    private lateinit var input_nome_dieta: EditText
    private var fazenda_corrente_id: String = "-1"
    private var fazenda_corrente_nome: String = "-1"
    private var lotes_nomes: ArrayList<String> = ArrayList()
    private var lotes_ids: ArrayList<String> = ArrayList()
    private lateinit var spinner: Spinner
    private lateinit var lote_selecionado_id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_dieta)

        val it = intent
        lote_selecionado_id = it.getStringExtra("lote_selecionado_id") ?: "-1"

        input_nome_dieta = findViewById(R.id.cadastrar_nome_da_dieta)
        progressBar = findViewById(R.id.progress_bar)
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1") ?: "-1"
        fazenda_corrente_nome = sharedpreferences.getString("fazenda_corrente_nome", "-1") ?: ""
        spinner = findViewById(R.id.spn_cadastrar_dieta)
        configura_listView()
        atualiza_lista_ingredientes()
        configura_toolbar()
        configura_spinner()
    }

    private fun atualiza_lista_ingredientes() { //usado para a criacao do primeiro documento dos possiveis ingredientes
/*
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Milho", new Ingrediente("Milho"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Caroço de algodão", new Ingrediente("Caroço de algodão"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Farelo de Soja", new Ingrediente("Farelo de Soja"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Feno", new Ingrediente("Feno"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Silagem", new Ingrediente("Silagem"));

         */
        progressBar.visibility = View.VISIBLE
        // busca no sharedPreferences caso nao tenha passado TIMEOUT_DB desde a ultima consulta no db
        if (System.currentTimeMillis() - sharedpreferences.getLong(
                "ingredientes_nomes_last_update",
                0
            ) < TIMEOUT_DB
        ) {
            val ing_aux = sharedpreferences.getString("ingredientes_nomes", null)
            if (ing_aux != null) {
                ingredientes_nomes = ArrayList(ing_aux.split(";;;"))
            }
            val itemsAdapter = AdapterIngredienteNome(this, ingredientes_nomes)
            listView_editar_ingredientes.adapter = itemsAdapter
            progressBar.visibility = View.GONE
        } else { // recebe os possiveis ingredientes
            ingredientes_nomes = ArrayList()
            DataBaseUtil.getDocumentsWhereEqualTo("ingredientes", "ativo", true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result
                        if (documents != null) {
                            for (document in documents) {
                                ingredientes_nomes.add(document.toObject<Ingrediente>(Ingrediente::class.java).nome)
                            }
                            // salva no sharedpreferences por um tempo para evitar muitos acessos no DB
                            val possiveis_ingredientes_string =
                                TextUtils.join(";;;", ingredientes_nomes)
                            val editor = sharedpreferences.edit()
                            editor.putString("ingredientes_nomes", possiveis_ingredientes_string)
                            editor.putLong(
                                "ingredientes_nomes_last_update",
                                System.currentTimeMillis()
                            )
                            editor.apply()
                        }
                    }
                    val itemsAdapter =
                        AdapterIngredienteNome(this@CadastrarDieta, ingredientes_nomes)
                    listView_editar_ingredientes.adapter = itemsAdapter
                    progressBar.visibility = View.GONE
                }
        }
    }

    private fun configura_toolbar() { // adiciona a barra de tarefas na tela
        val my_toolbar = findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(my_toolbar)
        // adiciona a seta de voltar na barra de tarefas
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Fazenda: $fazenda_corrente_nome"
    }

    fun salvar_dieta(view: View?) {
        if (listView_editar_ingredientes.checkedItemCount == 0) {
            show(this, "Selecione pelo menos um ingrediente", Toast.LENGTH_SHORT)
            return
        }
        if (!validaDados()) {
            return
        }

        progressBar.visibility = View.VISIBLE
        dieta = Dieta()
        dieta.nome = input_nome_dieta.text.toString()
        dieta.fazenda_id = fazenda_corrente_id
        dieta.dono_uid = getCurrentUser()?.uid ?: "-1"

        val spinner_pos = spinner.selectedItemPosition

        if (spinner_pos != 0) {
            dieta.lote_id = lotes_ids[spinner_pos]
        }

        val len = listView_editar_ingredientes.count
        val checked = listView_editar_ingredientes.checkedItemPositions
        for (i in 0 until len) {
            if (checked[i]) {
                val item = ingredientes_nomes[i]
                dieta.ingredientes_nomes?.add(item)
            }
        }
        // desativa a dieta anterior
//if (dieta_ativa_id != null) {
//    Log.i("MY_FIRESTORE", " " + dieta_ativa_id);
//    DataBaseUtil.getInstance().updateDocument("dietas", dieta_ativa_id, "ativo", false);
//}
// salva a nova dieta
        DataBaseUtil.insertDocument("dietas", dieta.id, dieta)
        // envia a dieta para o fragment
        Principal.sendData("DietasFragment", "adiciona_dieta", dieta)
        Handler().postDelayed({ finaliza_cadastro() }, 800)
    }

    private fun finaliza_cadastro() {
        show(this, "Dieta editada com sucesso!", Toast.LENGTH_SHORT)
        progressBar.visibility = View.GONE
        val it = Intent()
        it.putExtra("dieta_cadastrada", dieta)
        setResult(1, it)
        finish()
    }

    private fun validaDados(): Boolean {
        var valido = true
        val nome_dieta = input_nome_dieta.text.toString()
        if (TextUtils.isEmpty(nome_dieta)) {
            input_nome_dieta.error = "Campo necessário."
            valido = false
        } else {
            input_nome_dieta.error = null
        }
        return valido
    }

    private fun configura_listView() {
        listView_editar_ingredientes =
            findViewById(R.id.listView_cadastrar_dieta)
        listView_editar_ingredientes.choiceMode = AbsListView.CHOICE_MODE_MULTIPLE
        listView_editar_ingredientes.setOnItemClickListener { adapterView, view, i, l ->
            try {
                val imm =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            } catch (e: Exception) { // TODO: handle exception
            }
            if (listView_editar_ingredientes.isItemChecked(i)) {
                view.findViewById<View>(R.id.listView_poss_ing_add).visibility = View.GONE
                view.findViewById<View>(R.id.listView_poss_ing_remove).visibility = View.VISIBLE
            } else {
                view.findViewById<View>(R.id.listView_poss_ing_remove).visibility = View.GONE
                view.findViewById<View>(R.id.listView_poss_ing_add).visibility = View.VISIBLE
            }
        }
    }

    private fun configura_spinner() {
        DataBaseUtil.getDocumentsWhereEqualTo("lotes", "fazenda_id", fazenda_corrente_id)
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                if (task.isSuccessful) {
                    lotes_nomes = ArrayList()
                    lotes_nomes.add("Selecione")
                    lotes_ids = ArrayList()
                    lotes_ids.add("-1") // ignora a 1 posicao

                    val documents = task.result
                    if (documents != null) {
                        for (document in documents) {
                            lotes_nomes.add(
                                document.toObject(
                                    Lote::class.java
                                ).nome
                            )
                            lotes_ids.add(document.toObject(Lote::class.java).id)
                        }
                        val spn_adapter = ArrayAdapter(
                            this@CadastrarDieta,
                            android.R.layout.simple_spinner_dropdown_item,
                            lotes_nomes
                        )

                        spinner.adapter = spn_adapter
                        if (!lote_selecionado_id.equals("-1")) {
                            spinner.setSelection(lotes_ids.indexOf(lote_selecionado_id))
                        }
                        //spinner.setSelection(fazendas_ids.indexOf(fazenda_corrente_id));
                    }
                } else {
                    Log.i(
                        "MY_FIRESTORE",
                        "Erro ao recuperar documentos Fazendas: " + task.exception
                    )
                }
            })
    }

    override fun onStart() {
        super.onStart()

        if (!UserUtil.isLogged()) {
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
}
