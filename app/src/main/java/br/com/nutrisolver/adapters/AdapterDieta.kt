package br.com.nutrisolver.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import br.com.nutrisolver.R
import br.com.nutrisolver.models.Dieta
import java.util.*

class AdapterDieta(private val act: Activity?) : BaseAdapter() {
    val listItems: ArrayList<Dieta> = ArrayList()

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

        val view = act.layoutInflater.inflate(R.layout.lista_dieta_item, parent, false)

        val dieta = listItems[position]

        val nome = view.findViewById(R.id.lista_dieta_titulo) as TextView

        nome.text = dieta.nome

        return view
    }

    fun addItem(item: Dieta) {
        this.listItems.add(item)
        this.notifyDataSetChanged()
    }

    fun clear() {
        listItems.clear()
        this.notifyDataSetChanged()
    }
}