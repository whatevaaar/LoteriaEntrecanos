package com.entrecanos.loteria.modelo

class Jugador(
    val uid: String = "",
    val nombre: String = "",
    var puntos: Int = 0,
    val planilla: MutableList<Carta> = mutableListOf()
)