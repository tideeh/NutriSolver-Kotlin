package br.com.nutrisolver.objects

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Fazenda{
    val id : String = UUID.randomUUID().toString()

    val dataCriacao : String = SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(Timestamp(System.currentTimeMillis()))

    var dono_uid : String = ""
    var nome : String = ""
}