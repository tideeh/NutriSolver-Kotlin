package br.com.nutrisolver.tools

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import br.com.nutrisolver.R
import br.com.nutrisolver.objects.Fazenda

class AdapterFazenda(private val act: Activity) : BaseAdapter() {
    private val list_items: MutableList<Fazenda> = mutableListOf()

    override fun getCount(): Int {
        return list_items.size
    }

    override fun getItem(position: Int): Any {
        return list_items[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    fun getItemIdString(pos: Int): String {
        return list_items[pos].id
    }

    fun getItemName(pos: Int): String {
        return list_items[pos].nome
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.i("MY_FAZENDA_ADAPTER", "getView: $position")
        val view = act.layoutInflater.inflate(R.layout.lista_fazenda_item, parent, false)

        val item = list_items[position]

        val nome = view.findViewById(R.id.lista_fazenda_titulo) as TextView

        nome.text = item.nome

        Log.i("MY_FAZENDA_ADAPTER", "getView nome: ${item.nome}")

        return view
    }

    fun addItem(item: Fazenda) {
        this.list_items.add(item)
        this.notifyDataSetChanged()
        Log.i("MY_FAZENDA_ADAPTER", "list size: ${list_items.size}")
    }

    fun clear() {
        list_items.clear()
        this.notifyDataSetChanged()
    }
}