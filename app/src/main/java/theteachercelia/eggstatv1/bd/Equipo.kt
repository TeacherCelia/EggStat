package theteachercelia.eggstatv1.bd

data class Equipo(
    val nombre_equipo: String = "", //ID
    val puntos_equipo: Int = 0, // puntos que sumarán los alumnos de cada grupo
    val url_imagen_equipo: String = "" // url de imagen de equipo
)
