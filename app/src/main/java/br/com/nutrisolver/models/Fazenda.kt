package br.com.nutrisolver.models

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class Fazenda {
    val id: String = UUID.randomUUID().toString()
    val dataCriacao: String =
        SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(Timestamp(System.currentTimeMillis()))

    var dono_uid: String = "-1"
    var nome: String = ""
}