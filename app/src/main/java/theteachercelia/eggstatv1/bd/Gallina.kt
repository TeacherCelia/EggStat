package theteachercelia.eggstatv1.bd

data class Gallina(
    val nombre_gallina: String = "",
    val raza: String = "",
    val fecha_nacimiento: String = "",
    val total_huevos: Int = 0, // se irá sumando conforme los alumnos registren
    val foto_url: String = "" // url de la foto subida a FirebaseStorage
)
