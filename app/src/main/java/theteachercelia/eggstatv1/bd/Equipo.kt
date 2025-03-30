package theteachercelia.eggstatv1.bd

data class Equipo(
    val id: String = "",
    val nombre_equipo: String = "",
    val puntos_equipo: Int = 0 // puntos que sumarán los alumnos de cada grupo
)
