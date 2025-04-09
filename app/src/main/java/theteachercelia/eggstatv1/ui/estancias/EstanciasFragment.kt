package theteachercelia.eggstatv1.ui.estancias

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.utils.Utils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EstanciasFragment : Fragment() {

    private lateinit var viewModel: EstanciasViewModel

    // bd
    val firebaseAuth = FirebaseAuth.getInstance()
    val firebaseDB = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_estancias, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ---- instanciamos viewmodel
        viewModel = ViewModelProvider(this)[EstanciasViewModel::class.java]



        // ---- referencias a views
        // bebedero
        val imgBebedero = view.findViewById<ImageView>(R.id.img_bebedero)
        val txtEstadoBebedero = view.findViewById<TextView>(R.id.txt_estadoBebedero)
        val txtRevisionBebedero = view.findViewById<TextView>(R.id.txt_revisionBebedero)
        val txtUltimoUsuarioBebedero = view.findViewById<TextView>(R.id.txt_ultimoUsuarioBebedero)

        // comedero
        val imgComedero = view.findViewById<ImageView>(R.id.img_comedero)
        val txtEstadoComedero = view.findViewById<TextView>(R.id.txt_estadoComedero)
        val txtRevisionComedero = view.findViewById<TextView>(R.id.txt_revisionComedero)
        val txtUltimoUsuarioComedero = view.findViewById<TextView>(R.id.txt_ultimoUsuarioComedero)

        // gallinero
        val imgGallinero = view.findViewById<ImageView>(R.id.img_gallinero)
        val txtEstadoGallinero = view.findViewById<TextView>(R.id.txt_estadoGallinero)
        val txtRevisionGallinero = view.findViewById<TextView>(R.id.txt_revisionGallinero)
        val txtUltimoUsuarioGallinero = view.findViewById<TextView>(R.id.txt_ultimoUsuarioGallinero)

        // ---- observadores

        viewModel.mapaEstancias.observe(viewLifecycleOwner) { mapa ->
            //accedemos a los datos de cada estancia mediante su clave
            val bebedero = mapa["bebedero"]
            val comedero = mapa["comedero"]
            val gallinero = mapa["gallinero"]

            bebedero?.let {
                // el dato "estado" variará dependiendo de la fecha de la ultima revisión, así que se crea metodo para ello
                val estado = calcularEstadoEstancia(it.timestamp_ultima_revision, it.recurrencia_revision, it.tipo)
                txtEstadoBebedero.text = "Estado: $estado"
                txtRevisionBebedero.text = "Última revisión: ${convertirTimestamp(it.timestamp_ultima_revision)}"
                txtUltimoUsuarioBebedero.text = "Revisado por: ${it.ultimo_usuario}"
            }

            comedero?.let {
                val estado = calcularEstadoEstancia(it.timestamp_ultima_revision, it.recurrencia_revision, it.tipo)
                txtEstadoComedero.text = "Estado: $estado"
                txtRevisionComedero.text = "Última revisión: ${convertirTimestamp(it.timestamp_ultima_revision)}"
                txtUltimoUsuarioComedero.text = "Revisado por: ${it.ultimo_usuario}"
            }

            gallinero?.let {
                val estado = calcularEstadoEstancia(it.timestamp_ultima_revision, it.recurrencia_revision, it.tipo)
                txtEstadoGallinero.text = "Estado: $estado"
                txtRevisionGallinero.text = "Última revisión: ${convertirTimestamp(it.timestamp_ultima_revision)}"
                txtUltimoUsuarioGallinero.text = "Revisado por: ${it.ultimo_usuario}"
            }
        }

        // ---- lógica UI (listeners, etc)

        imgBebedero.setOnClickListener{
            //TODO: revisar
            mostrarDialogRevisionEstancia("bebedero", 10)
        }

        /*************
        BOTÓN COMEDERO
         *************/
        imgComedero.setOnClickListener{
            //TODO: implementar clic en imgcomedero
            mostrarDialogRevisionEstancia("comedero", 10)
        }

        /*************
        BOTÓN GALLINERO
         *************/
        imgGallinero.setOnClickListener{
            //TODO: implementar clic en imggallinero
            mostrarDialogRevisionEstancia("gallinero", 50)
        }

    }

    // ---- otros métodos

    // metodo para convertir el timestamp en fecha controlando errores
    private fun convertirTimestamp(timestamp: Long): String {
        return try {
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timestampFecha = Date(timestamp)
            formatoFecha.format(timestampFecha)
        } catch (e: Exception) {
            "Fecha no válida"
        }
    }

    // metodo para calcular el estado de la estancia (lleno/medio/vacio)
    private fun calcularEstadoEstancia(timestampUltimaRevision: Long, recurrenciaRevision: Double, tipoEstancia: String): String {
        val ahora = System.currentTimeMillis()
        val milisPorDia = 86400000 //24h * 60min * 60seg * 1000milis
        val diasPasados = (ahora - timestampUltimaRevision).toDouble() / milisPorDia

        return if (tipoEstancia == "gallinero") {
            when {
                diasPasados < recurrenciaRevision * 0.5 -> "limpio"
                diasPasados < recurrenciaRevision -> "decente"
                else -> "sucio"
            }
        } else {
            when {
                diasPasados < recurrenciaRevision * 0.5 -> "lleno"
                diasPasados < recurrenciaRevision -> "medio"
                else -> "vacío"
            }
        }
    }

    // metodo para cambiar dinámicamente el texto del dialog, dependiendo del boton que se pulse, y cambiar asi los datos de la BD
    private fun mostrarDialogRevisionEstancia(
        tipoEstancia: String,
        puntos: Int
    ) {
        val context = requireContext()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_registro_estancia, null)

        val uid = firebaseAuth.currentUser?.uid ?: return
        val nombreRef = firebaseDB.child("usuarios").child(uid).child("nombre_usuario")

        nombreRef.get().addOnSuccessListener { snapshot ->
            val nombreUsuario = snapshot.getValue(String::class.java) ?: "Usuario"

            // Actualizamos texto dinámico
            val txtUsuarioDialog = dialogView.findViewById<TextView>(R.id.txt_usuarioRegistroEstancia)
            val txtMensajeDialog = dialogView.findViewById<TextView>(R.id.txt_registroEstancia)
            txtUsuarioDialog.text = "Hola, $nombreUsuario"
            txtMensajeDialog.text = "¿Confirmas que has revisado el $tipoEstancia?"

            //abrimos dialog
            AlertDialog.Builder(context)
                .setTitle("Confirmar revisión")
                .setView(dialogView)
                .setPositiveButton("Confirmo") { _, _ ->
                    val timestampActual = System.currentTimeMillis()
                    val estanciaRef = firebaseDB.child("estancia").child(tipoEstancia)

                    estanciaRef.child("timestamp_ultima_revision").setValue(timestampActual)
                    estanciaRef.child("ultimo_usuario").setValue(nombreUsuario)

                    Utils.sumarPuntos(uid, puntos, firebaseDB)

                    //mostramos dialogo informativo (metodo de utils)
                    Utils.mostrarDialogoInformativo(requireContext(), tipoEstancia)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }



}