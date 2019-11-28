package br.com.nutrisolver.tools

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import br.com.nutrisolver.R
import br.com.nutrisolver.objects.Lote
import java.util.ArrayList

class AdapterLote(private val act : Activity?) : BaseAdapter() {
    val list_items: ArrayList<Lote> = ArrayList()

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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        if(act == null){
            return null
        }

        val view = act.layoutInflater.inflate(R.layout.lista_lote_item, parent, false)

        val lote = list_items[position]

        val nome = view.findViewById(R.id.lista_lote_titulo) as TextView

        nome.text = lote.nome

        return view
    }

    fun addItem(item: Lote) {
        this.list_items.add(item)
        this.notifyDataSetChanged()
    }

    fun clear() {
        list_items.clear()
        this.notifyDataSetChanged()
    }

}