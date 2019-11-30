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
import br.com.nutrisolver.utils.DataBaseUtil
import br.com.nutrisolver.utils.SP_NOME
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class DietasFragment : Fragment(),
    PrincipalActivity.DataFromActivityToFragment {
    lateinit var my_view: View
    private lateinit var listView_dietas: ListView
    private lateinit var adapterDieta: AdapterDieta
    private var lista_dietas: ArrayList<Dieta> = ArrayList()

    private var sharedpreferences: SharedPreferences? = null
    private var fazenda_corrente_id: String = "-1"
    private lateinit var progressBar: ProgressBar
    private var from_onSaveInstanceState = false

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

        this.my_view = view

        from_onSaveInstanceState = false
        lista_dietas = ArrayList()
        if (savedInstanceState != null) {
            lista_dietas =
                savedInstanceState.getParcelableArrayList<Dieta>("lista_dietas") ?: ArrayList()
            from_onSaveInstanceState = savedInstanceState.getBoolean("from_onSaveInstanceState")
        }

        sharedpreferences = activity?.getSharedPreferences(SP_NOME, Context.MODE_PRIVATE)
        fazenda_corrente_id = sharedpreferences?.getString("fazenda_corrente_id", "-1") ?: "-1"
        progressBar = view.findViewById(R.id.progress_bar)

        configura_listView()
        atualiza_lista_de_dietas()

        view.findViewById<FloatingActionButton>(R.id.fab_cadastrar_dieta).setOnClickListener {
            val ite = Intent(activity, CadastrarDietaActivity::class.java)
            startActivity(ite)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelableArrayList("lista_dietas", adapterDieta.list_items)
        outState.putBoolean("from_onSaveInstanceState", true)
    }

    private fun atualiza_lista_de_dietas() {
        progressBar.visibility = View.VISIBLE

        adapterDieta.clear()
        if (from_onSaveInstanceState) {
            from_onSaveInstanceState = false
            Log.i("MY_SAVED", "dietas vieram do saved!")
            for (dieta in lista_dietas) {
                adapterDieta.addItem(dieta)
            }
            progressBar.visibility = View.GONE
        } else {
            DataBaseUtil.getDocumentsWhereEqualTo(
                "dietas",
                arrayOf("fazenda_id", "ativo"),
                arrayOf(fazenda_corrente_id, true)
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

    private fun configura_listView() {
        listView_dietas = my_view.findViewById(R.id.lista_dietas) as ListView
        adapterDieta = AdapterDieta(activity)
        listView_dietas.adapter = adapterDieta
        listView_dietas.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                //Intent it = new Intent(view.getContext(), VisualizaDieta.class);
                //it.putExtra("lote_id", adapterLote.getItemIdString(position));
                //it.putExtra("lote_nome", adapterLote.getItemName(position));
                //startActivity(it);
            }
    }

    override fun sendData(data: String, `object`: Any?) {
        when (data) {
            "atualiza_dietas" -> {
                fazenda_corrente_id =
                    sharedpreferences?.getString("fazenda_corrente_id", "-1") ?: "-1"
                atualiza_lista_de_dietas()
            }

            "adiciona_dieta" -> {
                adapterDieta.addItem(`object` as Dieta)
            }

            else -> {
            }
        }
    }
}
