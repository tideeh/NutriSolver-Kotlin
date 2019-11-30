package br.com.nutrisolver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import br.com.nutrisolver.R

class AdapterIngredienteNome(
    context: Context,
    listPossiveisIngredientes: List<String>
) :
    ArrayAdapter<String?>(context, 0, listPossiveisIngredientes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val ing = getItem(position)

        var cv = convertView

        if (cv == null) {
            cv = LayoutInflater.from(context)
                .inflate(R.layout.lista_possiveis_ingredientes_item, parent, false)
        }

        val ingNome = cv?.findViewById<View>(R.id.lista_ingrediente_nome) as TextView

        ingNome.text = ing


        return cv
    }
}