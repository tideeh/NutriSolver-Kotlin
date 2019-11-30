package br.com.nutrisolver.models

import android.os.Parcel
import android.os.Parcelable
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Dieta() : Parcelable {
    var id: String = UUID.randomUUID().toString()
    var data_criacao: String =
        SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(Timestamp(System.currentTimeMillis()))

    var dono_uid: String = "-1"
    var fazenda_id: String = "-1"
    var lote_id: String = "-1"
    var nome: String = ""
    var isAtivo: Boolean = true
    var ingredientes_nomes: ArrayList<String>? =
        ArrayList() // tambem serve como DocumentReference pois o id do ingrediente eh o seu nome

    constructor(parcel: Parcel) : this() {
        id = parcel.readString() ?: "-1"
        data_criacao = parcel.readString() ?: "-1"
        dono_uid = parcel.readString() ?: "-1"
        fazenda_id = parcel.readString() ?: "-1"
        lote_id = parcel.readString() ?: "-1"
        nome = parcel.readString() ?: ""
        isAtivo = parcel.readByte() != 0.toByte()
        ingredientes_nomes = parcel.createStringArrayList()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(data_criacao)
        parcel.writeString(dono_uid)
        parcel.writeString(fazenda_id)
        parcel.writeString(lote_id)
        parcel.writeString(nome)
        parcel.writeByte(if (isAtivo) 1 else 0)
        parcel.writeStringList(ingredientes_nomes)
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