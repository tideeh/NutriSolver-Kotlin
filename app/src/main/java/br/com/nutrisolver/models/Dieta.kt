package br.com.nutrisolver.models

import android.os.Parcel
import android.os.Parcelable
import br.com.nutrisolver.utils.DEFAULT_STRING_VALUE
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Dieta() : Parcelable {
    var id: String = UUID.randomUUID().toString()
    var dataCriacao: String =
        SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(Timestamp(System.currentTimeMillis()))

    var donoUid: String = DEFAULT_STRING_VALUE
    var fazendaId: String = DEFAULT_STRING_VALUE
    var loteId: String = DEFAULT_STRING_VALUE
    var nome: String = DEFAULT_STRING_VALUE
    var ativo: Boolean = true
    var ingredientesNomes: ArrayList<String>? =
        ArrayList() // tambem serve como DocumentReference pois o id do ingrediente eh o seu nome

    constructor(parcel: Parcel) : this() {
        id = parcel.readString() ?: DEFAULT_STRING_VALUE
        dataCriacao = parcel.readString() ?: DEFAULT_STRING_VALUE
        donoUid = parcel.readString() ?: DEFAULT_STRING_VALUE
        fazendaId = parcel.readString() ?: DEFAULT_STRING_VALUE
        loteId = parcel.readString() ?: DEFAULT_STRING_VALUE
        nome = parcel.readString() ?: DEFAULT_STRING_VALUE
        ativo = parcel.readByte() != 0.toByte()
        ingredientesNomes = parcel.createStringArrayList()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(dataCriacao)
        parcel.writeString(donoUid)
        parcel.writeString(fazendaId)
        parcel.writeString(loteId)
        parcel.writeString(nome)
        parcel.writeByte(if (ativo) 1 else 0)
        parcel.writeStringList(ingredientesNomes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Dieta> {
        override fun createFromParcel(parcel: Parcel): Dieta {
            return Dieta(parcel)
        }

        override fun newArray(size: Int): Array<Dieta?> {
            return arrayOfNulls(size)
        }
    }


}