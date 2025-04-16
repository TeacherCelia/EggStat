package theteachercelia.eggstatv1.utils

import android.app.AlertDialog
import android.content.Context
import com.google.firebase.database.DatabaseReference
import theteachercelia.eggstatv1.R

object Utils {

    //metodo para sumar puntos a los usuarios y equipos
    fun sumarPuntos(uid: String, puntos: Int, firebaseDB: DatabaseReference){
        // buscamos el nodo del usuario en firebase
        val usuarioRef = firebaseDB.child("usuarios").child(uid)

        // leemos los puntos actuales y sumamos los nuevos puntos, guardando resultado con setValue
        usuarioRef.child("puntos_usuario").get().addOnSuccessListener { snapshot ->
            val puntosActuales = snapshot.getValue(Int::class.java) ?: 0
            usuarioRef.child("puntos_usuario").setValue(puntosActuales + puntos)
        }

        // sumamos puntos al equipo del usuario
        usuarioRef.child("equipo_id").get().addOnSuccessListener { equipoSnap ->
            val equipoID = equipoSnap.getValue(String::class.java)?.lowercase() // para que el ID coincida on el del nodo

            // buscamos el equipo en la BD y sumamos los mismos puntos
            if (!equipoID.isNullOrEmpty()) {
                val equipoRef = firebaseDB.child("equipo").child(equipoID)
                equipoRef.child("puntos_equipo").get().addOnSuccessListener { equipoPtsSnap ->
                    val puntosEquipoActuales = equipoPtsSnap.getValue(Int::class.java) ?: 0
                    equipoRef.child("puntos_equipo").setValue(puntosEquipoActuales + puntos)
                }
            }
        }
    }

    //metodo para mostrar dialogo informativo; usado en EstanciasFragment
    fun mostrarDialogoInformativo(
        context: Context,
        tipoEstancia: String
    ) {
        AlertDialog.Builder(context)
            .setTitle("¡$tipoEstancia revisado!")
            .setMessage("¡¡$tipoEstancia al día!! Las gallinas son un poquito más felices ahora :)")
            .setPositiveButton("Cerrar", null)
            .show()
    }

    //metodo para obtener colores personalizados; usado en EstadisticasFragment
    fun obtenerColoresPersonalizados(context: Context): List<Int> {
        return listOf(
            context.getColor(R.color.rojo),
            context.getColor(R.color.naranja),
            context.getColor(R.color.amarillo),
            context.getColor(R.color.verde),
            context.getColor(R.color.azul_verde_claro),
            context.getColor(R.color.azul_verdoso)
        )
    }
}