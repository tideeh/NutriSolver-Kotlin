package br.com.nutrisolver.activitys


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import br.com.nutrisolver.R
import br.com.nutrisolver.objects.Lote
import br.com.nutrisolver.tools.AdapterLote
import br.com.nutrisolver.tools.DataBaseUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.QuerySnapshot
import java.util.ArrayList

class LotesFragment : Fragment(), Principal.DataFromActivityToFragment {

    lateinit var my_view : View
    private lateinit var listView_lotes: ListView
    private lateinit var adapterLote: AdapterLote
    private var lista_lotes: ArrayList<Lote> = ArrayList()

    private var sharedpreferences: SharedPreferences? = null
    private var fazenda_corrente_id: String = "-1"
    private lateinit var progressBar: ProgressBar
    private var from_onSaveInstanceState = false

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

        this.my_view = view

        from_onSaveInstanceState = false
        lista_lotes = ArrayList()
        if (savedInstanceState != null) {
            lista_lotes = savedInstanceState.getParcelableArrayList<Lote>("lista_lotes") ?: ArrayList()
            from_onSaveInstanceState = savedInstanceState.getBoolean("from_onSaveInstanceState")
        }

        sharedpreferences = activity?.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        fazenda_corrente_id = sharedpreferences?.getString("fazenda_corrente_id", "-1") ?: "-1"
        progressBar = view.findViewById(R.id.progress_bar)

        configura_listView()
        atualiza_lista_de_lotes()

        view.findViewById<FloatingActionButton>(R.id.fab_cadastrar_lote).setOnClickListener {
            val ite = Intent(activity, CadastrarLote::class.java)
            startActivity(ite)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelableArrayList("lista_lotes", adapterLote.list_items)
        outState.putBoolean("from_onSaveInstanceState", true)
    }

    private fun atualiza_lista_de_lotes() {
        progressBar.visibility = View.VISIBLE

        adapterLote.clear()
        if (from_onSaveInstanceState) {
            from_onSaveInstanceState = false
            Log.i("MY_SAVED", "lotes vieram do saved!")
            for (lote in lista_lotes) {
                adapterLote.addItem(lote)
            }
            progressBar.visibility = View.GONE
        } else {
            DataBaseUtil.getDocumentsWhereEqualTo("lotes", "fazenda_id", fazenda_corrente_id)
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

    private fun configura_listView() {
        listView_lotes = my_view.findViewById(R.id.lista_lotes) as ListView
        adapterLote = AdapterLote(activity)
        listView_lotes.adapter = adapterLote
        listView_lotes.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val it = Intent(view.context, VisualizaLote::class.java)
                it.putExtra("lote_id", adapterLote.getItemIdString(position))
                it.putExtra("lote_nome", adapterLote.getItemName(position))
                startActivity(it)
            }
    }

    override fun sendData(data: String, `object`: Any?) {
        when(data){
            "atualiza_lotes" -> {
                Log.i("MY_SENDDATA", "atualiza_lotes")
                fazenda_corrente_id = sharedpreferences?.getString("fazenda_corrente_id", "-1") ?: "-1"
                atualiza_lista_de_lotes()
            }

            "adiciona_lote" -> {
                adapterLote.addItem(`object` as Lote)
            }

            else -> {
            }
        }
    }
}
