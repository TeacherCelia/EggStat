package theteachercelia.eggstatv1.bd

data class Gallina(
    val id: String = "",
    val nombre_gallina: String = "",
    val raza: String = "",
    val edad: Int = 0,
    val total_huevos: Int = 0, // se irá sumando conforme los alumnos registren
    val foto_url: String = "" // url de la foto subida a FirebaseStorage
)
