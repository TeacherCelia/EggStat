package theteachercelia.eggstatv1.bd

data class Estancia(
    val id: String = "",
    val id_usuario: String = "",
    val tipo: String = "", // comedero, bebedero, gallinero
    val recurrencia_revision: Int = 0, // días máximos sin revisar
    val estado: String = "", // limpio/decente/sucio - lleno/medio/vacío
    val fecha_ultima_revision: Long = 0L // timestamp en milisegundos para calcular
)
