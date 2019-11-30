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
import br.com.nutrisolver.activitys.CadastrarDietaActivity
import br.com.nutrisolver.activitys.PrincipalActivity
import br.com.nutrisolver.models.Dieta
import br.com.nutrisolver.adapters.AdapterDieta
import br.com.nutrisolver.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class DietasFragment : Fragment(),
    PrincipalActivity.DataFromActivityToFragment {
    private lateinit var myView: View
    private lateinit var listviewDietas: ListView
    private lateinit var adapterDieta: AdapterDieta
    private var listDietas: ArrayList<Dieta> = ArrayList()

    private var sharedPreferences: SharedPreferences? = null
    private var fazendaCorrenteId: String = DEFAULT_STRING_VALUE
    private lateinit var progressBar: ProgressBar
    private var fromOnSaveInstanceState = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("MY_TABS", "DietasFragment onCreateView")
        return inflater.inflate(R.layout.fragment_dietas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.myView = view

        fromOnSaveInstanceState = false
        listDietas = ArrayList()
        if (savedInstanceState != null) {
            listDietas =
                savedInstanceState.getParcelableArrayList<Dieta>(BUNDLE_KEY_LISTA_DIETAS) ?: ArrayList()
            fromOnSaveInstanceState = savedInstanceState.getBoolean(
                BUNDLE_KEY_FROM_ON_SAVE_INSTANCE_STATE_DIETAS)
        }

        sharedPreferences = activity?.getSharedPreferences(SP_NOME, Context.MODE_PRIVATE)
        fazendaCorrenteId = sharedPreferences?.getString(SP_KEY_FAZENDA_CORRENTE_ID, DEFAULT_STRING_VALUE) ?: DEFAULT_STRING_VALUE
        progressBar = view.findViewById(R.id.progress_bar)

        configuraListview()
        atualizaListaDeDietas()

        view.findViewById<FloatingActionButton>(R.id.fab_cadastrar_dieta).setOnClickListener {
            val ite = Intent(activity, CadastrarDietaActivity::class.java)
            startActivity(ite)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelableArrayList(BUNDLE_KEY_LISTA_DIETAS, adapterDieta.listItems)
        outState.putBoolean(BUNDLE_KEY_FROM_ON_SAVE_INSTANCE_STATE_DIETAS, true)
    }

    private fun atualizaListaDeDietas() {
        progressBar.visibility = View.VISIBLE

        adapterDieta.clear()
        if (fromOnSaveInstanceState) {
            fromOnSaveInstanceState = false
            Log.i("MY_SAVED", "dietas vieram do saved!")
            for (dieta in listDietas) {
                adapterDieta.addItem(dieta)
            }
            progressBar.visibility = View.GONE
        } else {
            DataBaseUtil.getDocumentsWhereEqualTo(
                DB_COLLECTION_DIETAS,
                arrayOf(Dieta::fazendaId.name, Dieta::ativo.name),
                arrayOf(fazendaCorrenteId, true)
            )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result
                        if (documents != null) {
                            for (document in documents) {
                                adapterDieta.addItem(document.toObject<Dieta>(Dieta::class.java))
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

        if (requestCode == CADASTRAR_DIETA_REQUEST && resultCode == 1) { // foi cadastrado um lote novo, adiciona ele na lista de lotes e atualizar o adapter da listView
            Dieta d = (Dieta) data.getParcelableExtra("dieta_cadastrada");
            Log.i("MY_ACTIVITY_RESULT", "Dieta nome: " + d.getNome());

            //adapterDieta.addItem(d);
        }
    }

 */

    private fun configuraListview() {
        listviewDietas = myView.findViewById(R.id.lista_dietas) as ListView
        adapterDieta = AdapterDieta(activity)
        listviewDietas.adapter = adapterDieta
        listviewDietas.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                //Intent it = new Intent(view.getContext(), VisualizaDieta.class);
                //it.putExtra("lote_id", adapterLote.getItemIdString(position));
                //it.putExtra("lote_nome", adapterLote.getItemName(position));
                //startActivity(it);
            }
    }

    override fun sendData(data: String, `object`: Any?) {
        when (data) {
            SEND_DATA_COMMAND_ATUALIZA_DIETAS -> {
                fazendaCorrenteId =
                    sharedPreferences?.getString(SP_KEY_FAZENDA_CORRENTE_ID, DEFAULT_STRING_VALUE) ?: DEFAULT_STRING_VALUE
                atualizaListaDeDietas()
            }

            SEND_DATA_COMMAND_ADICIONA_DIETA -> {
                adapterDieta.addItem(`object` as Dieta)
            }

            else -> {
            }
        }
    }
}
