package theteachercelia.eggstatv1.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import theteachercelia.eggstatv1.R

object Utils {
    /*

    Clase object creada para dejar el código del resto de clases
    más limpio y reutilizar métodos en algunas de ellas

     */

    // ------------------------------------------- //
    // ---- SUMAR PUNTOS A USUARIOS Y EQUIPOS ---- //
    // ------------------------------------------- //

    // suma puntos al añadir un huevo o revisar una estancia, a usuarios y equipos

    fun sumarPuntos(
        uid: String,
        puntos: Int,
        firebaseDB: DatabaseReference){

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

    // --------------------------------------------- //
    // ---- CANJEAR PUNTOS A USUARIOS Y EQUIPOS ---- //
    // --------------------------------------------- //

    // resta los puntos a los usuarios o equipos desde el ControlFragment

    fun restarPuntos(
        id: String, // uid de usuario o nombre del equipo
        puntos: Int, // puntos que canjearemos (restar)
        firebaseDB: DatabaseReference, // referencia al nodo raiz de la BD
        tipo: String // "usuario" o "equipo"
    ) {
        // se elige el nodo según el tipo (usuario o equipo)
        val nodo =
            if (tipo == "usuario")
                "usuarios"
            else "equipo"

        // se elige el nombre del campo de puntos que se modificarán
        val campo =
            if (tipo == "usuario")
                "puntos_usuario"
            else "puntos_equipo"

        // referencia al usuario o equipo en Firebase
        val ref = firebaseDB.child(nodo).child(id)

        // lectura de los puntos que este usuario o equipo tiene
        ref.child(campo).get().addOnSuccessListener { snapshot ->
            // se obtiene la cantidad de puntos
            val puntosActuales = snapshot.getValue(Int::class.java) ?: 0
            // si hay suficientes puntos, se resta
            if (puntos <= puntosActuales) {
                ref.child(campo).setValue(puntosActuales - puntos)
            }
        }
    }

    // ------------------------------------- //
    // ---- MOSTRAR DIALOGO INFORMATIVO ---- //
    // ------------------------------------- //

    // metodo para mostrar dialogo informativo; usado en EstanciasFragment, Huevosfragment y avisos de error

    fun mostrarDialogoInformativo(
        context: Context, // contexto desde el que se llama el metodo
        mensaje: String, // texto que se mostrará en el dialog
        urlImagen: String // url de la imagen que se mostrará en el dialog con Glide (al ser un GIF)
    ) {
        // vista del dialogo desde dialog_informativo.xml
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_informativo, null)

        // se referencian todas las partes del dialog_informativo.xml
        val txtMensaje = dialogView.findViewById<TextView>(R.id.txt_dialog)
        val imgGif = dialogView.findViewById<ImageView>(R.id.img_dialogInformativo)

        txtMensaje.text = mensaje

        // libreria Glide para cargar imagenes
        Glide.with(context)
            .asGif() // como animacion GIF
            .load(urlImagen)
            .into(imgGif)

        // se muestra el alertdialog con la vista personalizada
        AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    // ---------------------------------------- //
    // ---- OBTENER COLORES PERSONALIZADOS ---- //
    // ---------------------------------------- //

    // devuelve una lista con mis colores personalizados, para usarlos en la librería de estadísticas

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

    // ------------------------------------ //
    // ---- MOSTRAR UN PROGRESS DIALOG ---- //
    // ------------------------------------ //

    fun mostrarDialogoCargando(context: Context, mensaje: String): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_cargando, null)
        val txtMensaje = view.findViewById<TextView>(R.id.txt_MensajeCarga)
        txtMensaje.text = mensaje

        builder.setView(view)
        builder.setCancelable(false) // el usuario no puede cerrarlo manualmente
        val dialog = builder.create()
        dialog.show()
        return dialog
    }

}