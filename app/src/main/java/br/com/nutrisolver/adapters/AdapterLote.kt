package br.com.nutrisolver.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import br.com.nutrisolver.R
import br.com.nutrisolver.models.Lote
import java.util.*

class AdapterLote(private val act: Activity?) : BaseAdapter() {
    val listItems: ArrayList<Lote> = ArrayList()

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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        if (act == null) {
            return null
        }

        val view = act.layoutInflater.inflate(R.layout.lista_lote_item, parent, false)

        val lote = listItems[position]

        val nome = view.findViewById(R.id.lista_lote_titulo) as TextView

        nome.text = lote.nome

        return view
    }

    fun addItem(item: Lote) {
        this.listItems.add(item)
        this.notifyDataSetChanged()
    }

    fun clear() {
        listItems.clear()
        this.notifyDataSetChanged()
    }

}