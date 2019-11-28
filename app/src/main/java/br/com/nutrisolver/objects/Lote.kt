package br.com.nutrisolver.objects

import android.os.Parcel
import android.os.Parcelable
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class Lote() : Parcelable {
    var id : String = UUID.randomUUID().toString()
    var data_criacao: String = SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(Timestamp(System.currentTimeMillis()))

    var dono_uid: String = "-1"
    var fazenda_id: String = "-1"
    var nome: String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readString() ?: "-1"
        data_criacao = parcel.readString() ?: "-1"
        dono_uid = parcel.readString() ?: "-1"
        fazenda_id = parcel.readString() ?: "-1"
        nome = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(data_criacao)
        parcel.writeString(dono_uid)
        parcel.writeString(fazenda_id)
        parcel.writeString(nome)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Lote> {
        override fun createFromParcel(parcel: Parcel): Lote {
            return Lote(parcel)
        }

        override fun newArray(size: Int): Array<Lote?> {
            return arrayOfNulls(size)
        }
    }
}