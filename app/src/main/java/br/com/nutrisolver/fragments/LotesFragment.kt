package br.com.nutrisolver.fragments


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import br.com.nutrisolver.R
import br.com.nutrisolver.activitys.CadastrarLoteActivity
import br.com.nutrisolver.activitys.PrincipalActivity
import br.com.nutrisolver.activitys.VisualizarLoteActivity
import br.com.nutrisolver.models.Lote
import br.com.nutrisolver.adapters.AdapterLote
import br.com.nutrisolver.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class LotesFragment : Fragment(),
    PrincipalActivity.DataFromActivityToFragment {

    private lateinit var myView: View
    private lateinit var listviewLotes: ListView
    private lateinit var adapterLote: AdapterLote
    private var listlotes: ArrayList<Lote> = ArrayList()

    private var sharedPreferences: SharedPreferences? = null
    private var fazendaCorrenteId: String = DEFAULT_STRING_VALUE
    private lateinit var progressBar: ProgressBar
    private var fromOnSaveInstanceState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MY_TABS", "LotesFragment onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("MY_TABS", "LotesFragment onCreateView")
        return inflater.inflate(R.layout.fragment_lotes, container, false)
    }

    // aqui a view ja esta criada e nao tem risco de nullpoint
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.myView = view

        fromOnSaveInstanceState = false
        listlotes = ArrayList()
        if (savedInstanceState != null) {
            listlotes =
                savedInstanceState.getParcelableArrayList<Lote>(BUNDLE_KEY_LISTA_LOTES)
                    ?: ArrayList()
            fromOnSaveInstanceState = savedInstanceState.getBoolean(
                BUNDLE_KEY_FROM_ON_SAVE_INSTANCE_STATE_LOTES
            )
        }

        sharedPreferences = activity?.getSharedPreferences(SP_NOME, Context.MODE_PRIVATE)
        fazendaCorrenteId =
            sharedPreferences?.getString(SP_KEY_FAZENDA_CORRENTE_ID, DEFAULT_STRING_VALUE)
                ?: DEFAULT_STRING_VALUE
        progressBar = view.findViewById(R.id.progress_bar)

        configuraListview()
        atualizaListaDeLotes()

        view.findViewById<FloatingActionButton>(R.id.fab_cadastrar_lote).setOnClickListener {
            val ite = Intent(activity, CadastrarLoteActivity::class.java)
            startActivity(ite)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelableArrayList(BUNDLE_KEY_LISTA_LOTES, adapterLote.listItems)
        outState.putBoolean(BUNDLE_KEY_FROM_ON_SAVE_INSTANCE_STATE_LOTES, true)
    }

    private fun atualizaListaDeLotes() {
        progressBar.visibility = View.VISIBLE

        adapterLote.clear()
        if (fromOnSaveInstanceState) {
            fromOnSaveInstanceState = false
            Log.i("MY_SAVED", "lotes vieram do saved!")
            for (lote in listlotes) {
                adapterLote.addItem(lote)
            }
            progressBar.visibility = View.GONE
        } else {
            DataBaseUtil.getDocumentsWhereEqualTo(
                DB_COLLECTION_LOTES,
                arrayOf(Lote::fazendaId.name, Lote::ativo.name),
                arrayOf(fazendaCorrenteId, true)
            )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result
                        if (documents != null) {
                            for (document in documents) {
                                adapterLote.addItem(document.toObject<Lote>(Lote::class.java))
                                Log.i(
                                    "MY_FIRESTORE",
                                    "lotes do db: " + document.toObject<Lote>(Lote::class.java).nome
                                )
                            }
                        }
                        progressBar.visibility = View.GONE
                    } else {
                        Log.i("MY_FIRESTORE", "Error getting documents: " + task.exception)
                        progressBar.visibility = View.GONE
                    }
                }
        }
    }
    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CADASTRAR_LOTE_REQUEST && resultCode == 1) { // foi cadastrado um lote novo, adiciona ele na lista de lotes e atualizar o adapter da listView
            Lote l = (Lote) data.getParcelableExtra("lote_cadastrado");
            Log.i("MY_ACTIVITY_RESULT", "lote nome: " + l.getNome());

            adapterLote.addItem(l);
        }
    }

 */

    private fun configuraListview() {
        listviewLotes = myView.findViewById(R.id.lista_lotes) as ListView
        adapterLote = AdapterLote(activity)
        listviewLotes.adapter = adapterLote
        listviewLotes.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val it = Intent(view.context, VisualizarLoteActivity::class.java)
                it.putExtra(INTENT_KEY_LOTE_SELECIONADO_ID, adapterLote.getItemIdString(position))
                it.putExtra(INTENT_KEY_LOTE_SELECIONADO_NOME, adapterLote.getItemName(position))
                startActivity(it)
            }
    }

    override fun sendData(data: String, `object`: Any?) {
        when (data) {
            SEND_DATA_COMMAND_ATUALIZA_LOTES -> {
                Log.i("MY_SENDDATA", "atualiza_lotes")
                fazendaCorrenteId =
                    sharedPreferences?.getString(SP_KEY_FAZENDA_CORRENTE_ID, DEFAULT_STRING_VALUE)
                        ?: DEFAULT_STRING_VALUE
                atualizaListaDeLotes()
            }

            SEND_DATA_COMMAND_ADICIONA_LOTE -> {
                adapterLote.addItem(`object` as Lote)
            }

            else -> {
            }
        }
    }
}
