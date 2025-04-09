package theteachercelia.eggstatv1.bd

data class Usuario(
    val nombre_usuario: String = "",
    val email: String = "", //ID
    val equipo_id: String = "",
    val rol: String = "", // alumno o profesor
    val puntos_usuario: Int = 0, // puntos acumulados por limpiar etc
)
