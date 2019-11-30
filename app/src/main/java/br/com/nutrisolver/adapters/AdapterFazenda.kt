package br.com.nutrisolver.adapters

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import br.com.nutrisolver.R
import br.com.nutrisolver.models.Fazenda

class AdapterFazenda(private val act: Activity) : BaseAdapter() {
    private val listItems: MutableList<Fazenda> = mutableListOf()

    override fun getCount(): Int {
        return listItems.size
    }

    override fun getItem(position: Int): Any {
        return listItems[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    fun getItemIdString(pos: Int): String {
        return listItems[pos].id
    }

    fun getItemName(pos: Int): String {
        return listItems[pos].nome
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.i("MY_FAZENDA_ADAPTER", "getView: $position")
        val view = act.layoutInflater.inflate(R.layout.lista_fazenda_item, parent, false)

        val item = listItems[position]

        val nome = view.findViewById(R.id.lista_fazenda_titulo) as TextView

        nome.text = item.nome

        Log.i("MY_FAZENDA_ADAPTER", "getView nome: ${item.nome}")

        return view
    }

    fun addItem(item: Fazenda) {
        this.listItems.add(item)
        this.notifyDataSetChanged()
        Log.i("MY_FAZENDA_ADAPTER", "list size: ${listItems.size}")
    }

    fun clear() {
        listItems.clear()
        this.notifyDataSetChanged()
    }
}