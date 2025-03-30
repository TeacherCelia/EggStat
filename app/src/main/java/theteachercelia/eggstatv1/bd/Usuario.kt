package theteachercelia.eggstatv1.bd

data class Usuario(
    val id: String = "",
    val equipo_id: String = "",
    val nombre_usuario: String = "",
    val rol: String = "", // alumno o profesor
    val puntos_usuario: Int = 0 // puntos acumulados por limpiar
)
