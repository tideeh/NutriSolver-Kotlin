package br.com.nutrisolver.activitys

import android.app.Activity
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
import br.com.nutrisolver.models.Dieta
import br.com.nutrisolver.models.Ingrediente
import br.com.nutrisolver.models.Lote
import br.com.nutrisolver.adapters.AdapterIngredienteNome
import br.com.nutrisolver.utils.*
import br.com.nutrisolver.utils.ToastUtil.show
import br.com.nutrisolver.utils.UserUtil.getCurrentUser

class CadastrarDietaActivity : AppCompatActivity() {
    //private val TIMEOUT_DB = 30 * 60 * 1000.toLong() // ms (MIN * 60 * 100)
    private var listIngredientesNomes: ArrayList<String> = ArrayList()
    private lateinit var dieta: Dieta
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressBar: ProgressBar
    private lateinit var listViewEditarIngredientes: ListView
    private lateinit var editTextNomeDieta: EditText
    private var fazendaCorrenteId: String = DEFAULT_STRING_VALUE
    private var fazendaCorrenteNome: String = DEFAULT_STRING_VALUE
    private var listLotesNomes: ArrayList<String> = ArrayList()
    private var listLotesIds: ArrayList<String> = ArrayList()
    private lateinit var spinner: Spinner
    private var loteSelecionadoId: String = DEFAULT_STRING_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_dieta)

        val it = intent
        loteSelecionadoId = it.getStringExtra(INTENT_KEY_LOTE_SELECIONADO_ID) ?: DEFAULT_STRING_VALUE

        editTextNomeDieta = findViewById(R.id.cadastrar_nome_da_dieta)
        progressBar = findViewById(R.id.progress_bar)
        sharedPreferences = getSharedPreferences(SP_NOME, Context.MODE_PRIVATE)
        fazendaCorrenteId = sharedPreferences.getString(SP_KEY_FAZENDA_CORRENTE_ID, DEFAULT_STRING_VALUE) ?: DEFAULT_STRING_VALUE
        fazendaCorrenteNome = sharedPreferences.getString(SP_KEY_FAZENDA_CORRENTE_NOME, DEFAULT_STRING_VALUE) ?: DEFAULT_STRING_VALUE
        Log.i("MY_CONSTANTS", fazendaCorrenteNome+" "+SP_KEY_FAZENDA_CORRENTE_NOME)
        spinner = findViewById(R.id.spn_cadastrar_dieta)
        configuraListview()
        atualizaListaIngredientes()
        configuraToolbar()
        configuraSpinner()
    }

    private fun atualizaListaIngredientes() { //usado para a criacao do primeiro documento dos possiveis ingredientes
/*
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Milho", new Ingrediente("Milho"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Caroço de algodão", new Ingrediente("Caroço de algodão"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Farelo de Soja", new Ingrediente("Farelo de Soja"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Feno", new Ingrediente("Feno"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Silagem", new Ingrediente("Silagem"));

         */
        progressBar.visibility = View.VISIBLE
        // busca no sharedPreferences caso nao tenha passado TIMEOUT_DB desde a ultima consulta no db
        if (System.currentTimeMillis() - sharedPreferences.getLong(
                SP_KEY_INGREDIENTES_NOMES_LAST_UPDATE,
                DEFAULT_LONG_VALUE
            ) < DB_TIMEOUT_TO_REQUEST
        ) {
            val ingredientesNomesTemp = sharedPreferences.getString(SP_KEY_INGREDIENTES_NOMES, null)
            if (ingredientesNomesTemp != null) {
                listIngredientesNomes = ArrayList(ingredientesNomesTemp.split(STRING_LIST_DELIMITER))
            }
            val itemsAdapter =
                AdapterIngredienteNome(
                    this,
                    listIngredientesNomes
                )
            listViewEditarIngredientes.adapter = itemsAdapter
            progressBar.visibility = View.GONE
        } else { // recebe os possiveis ingredientes
            listIngredientesNomes = ArrayList()
            DataBaseUtil.getDocumentsWhereEqualTo(DB_COLLECTION_INGREDIENTES, Ingrediente::ativo.name, true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result
                        if (documents != null) {
                            for (document in documents) {
                                listIngredientesNomes.add(document.toObject<Ingrediente>(Ingrediente::class.java).nome)
                            }
                            // salva no sharedpreferences por um tempo para evitar muitos acessos no DB
                            val ingredientesNomesTemp =
                                TextUtils.join(STRING_LIST_DELIMITER, listIngredientesNomes)
                            val editor = sharedPreferences.edit()
                            editor.putString(SP_KEY_INGREDIENTES_NOMES, ingredientesNomesTemp)
                            editor.putLong(
                                SP_KEY_INGREDIENTES_NOMES_LAST_UPDATE,
                                System.currentTimeMillis()
                            )
                            editor.apply()
                        }
                    }
                    val itemsAdapter =
                        AdapterIngredienteNome(
                            this@CadastrarDietaActivity,
                            listIngredientesNomes
                        )
                    listViewEditarIngredientes.adapter = itemsAdapter
                    progressBar.visibility = View.GONE
                }
        }
    }

    private fun configuraToolbar() { // adiciona a barra de tarefas na tela
        val myActionBar = findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(myActionBar)
        // adiciona a seta de voltar na barra de tarefas
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.fazenda)+" : $fazendaCorrenteNome"
    }

    fun salvarDieta(view: View?) {
        if (listViewEditarIngredientes.checkedItemCount == 0) {
            show(this, getString(R.string.selecione_pelo_menos_um_ingrediente), Toast.LENGTH_SHORT)
            return
        }
        if (!validaDados()) {
            return
        }

        progressBar.visibility = View.VISIBLE
        dieta = Dieta()
        dieta.nome = editTextNomeDieta.text.toString()
        dieta.fazendaId = fazendaCorrenteId
        dieta.donoUid = getCurrentUser()?.uid ?: DEFAULT_STRING_VALUE

        val spinnerPos = spinner.selectedItemPosition

        if (spinnerPos != 0) {
            dieta.loteId = listLotesIds[spinnerPos]
        }

        val len = listViewEditarIngredientes.count
        val checked = listViewEditarIngredientes.checkedItemPositions
        for (i in 0 until len) {
            if (checked[i]) {
                val item = listIngredientesNomes[i]
                dieta.ingredientesNomes?.add(item)
            }
        }
        // desativa a dieta anterior
//if (dieta_ativa_id != null) {
//    Log.i("MY_FIRESTORE", " " + dieta_ativa_id);
//    DataBaseUtil.getInstance().updateDocument("dietas", dieta_ativa_id, "ativo", false);
//}
// salva a nova dieta
        DataBaseUtil.insertDocument(DB_COLLECTION_DIETAS, dieta.id, dieta)
        // envia a dieta para o fragment
        PrincipalActivity.sendData(SEND_DATA_FRAGMENT_DIETAS, SEND_DATA_COMMAND_ADICIONA_DIETA, dieta)
        Handler().postDelayed({ finalizaCadastro() }, 800)
    }

    private fun finalizaCadastro() {
        show(this, getString(R.string.dieta_cadastrada_com_sucesso), Toast.LENGTH_SHORT)
        progressBar.visibility = View.GONE
        val it = Intent()
        it.putExtra(INTENT_KEY_DIETA_CADASTRADA, dieta)
        setResult(Activity.RESULT_OK, it)
        finish()
    }

    private fun validaDados(): Boolean {
        var valido = true
        val nomeDieta = editTextNomeDieta.text.toString()
        if (TextUtils.isEmpty(nomeDieta)) {
            editTextNomeDieta.error = getString(R.string.campo_necessario)
            valido = false
        } else {
            editTextNomeDieta.error = null
        }
        return valido
    }

    private fun configuraListview() {
        listViewEditarIngredientes =
            findViewById(R.id.listView_cadastrar_dieta)
        listViewEditarIngredientes.choiceMode = AbsListView.CHOICE_MODE_MULTIPLE
        listViewEditarIngredientes.setOnItemClickListener { adapterView, view, i, l ->
            try {
                val imm =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            } catch (e: Exception) { // TODO: handle exception
            }
            if (listViewEditarIngredientes.isItemChecked(i)) {
                view.findViewById<View>(R.id.listView_poss_ing_add).visibility = View.GONE
                view.findViewById<View>(R.id.listView_poss_ing_remove).visibility = View.VISIBLE
            } else {
                view.findViewById<View>(R.id.listView_poss_ing_remove).visibility = View.GONE
                view.findViewById<View>(R.id.listView_poss_ing_add).visibility = View.VISIBLE
            }
        }
    }

    private fun configuraSpinner() {
        DataBaseUtil.getDocumentsWhereEqualTo(DB_COLLECTION_LOTES, Lote::fazendaId.name, fazendaCorrenteId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    listLotesNomes = ArrayList()
                    listLotesNomes.add(getString(R.string.selecione))
                    listLotesIds = ArrayList()
                    listLotesIds.add(DEFAULT_STRING_VALUE) // ignora a 1 posicao

                    val documents = task.result
                    if (documents != null) {
                        for (document in documents) {
                            listLotesNomes.add(
                                document.toObject(
                                    Lote::class.java
                                ).nome
                            )
                            listLotesIds.add(document.toObject(Lote::class.java).id)
                        }
                        val spnAdapter = ArrayAdapter(
                            this@CadastrarDietaActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            listLotesNomes
                        )

                        spinner.adapter = spnAdapter
                        if (loteSelecionadoId != DEFAULT_STRING_VALUE) {
                            spinner.setSelection(listLotesIds.indexOf(loteSelecionadoId))
                        }
                        //spinner.setSelection(fazendas_ids.indexOf(fazenda_corrente_id));
                    }
                } else {
                    Log.i(
                        "MY_FIRESTORE",
                        "Erro ao recuperar documentos Fazendas: " + task.exception
                    )
                }
            }
    }

    override fun onStart() {
        super.onStart()

        if (!UserUtil.isLogged()) {
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
}
