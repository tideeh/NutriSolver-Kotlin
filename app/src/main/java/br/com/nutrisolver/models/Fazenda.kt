package br.com.nutrisolver.models

import br.com.nutrisolver.utils.DEFAULT_STRING_VALUE
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class Fazenda {
    val id: String = UUID.randomUUID().toString()
    val dataCriacao: String =
        SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(Timestamp(System.currentTimeMillis()))

    var donoUid: String = DEFAULT_STRING_VALUE
    var nome: String = DEFAULT_STRING_VALUE
    var ativo: Boolean = true
}