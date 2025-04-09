package theteachercelia.eggstatv1.bd

data class Estancia(
    val tipo: String = "", // / ID: comedero, bebedero, gallinero
    val recurrencia_revision: Double = 0.0, // días máximos sin revisar
    val timestamp_ultima_revision: Long = 0L, // timestamp en milisegundos para calcular
    val ultimo_usuario: String = ""
)
