package theteachercelia.eggstatv1.ui.estancias

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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

    /*
    Con este fragment se muestra el estado de las estancias (bebedero, comedero y gallinero)
    - Visualiza la ultima revisión de cada estancia y su estado actual (limpio/medio/sucio...)
    - Permite registrar nuevas revisiones, actualizando Firebase y sumando puntos al usuario
    - Obtiene los datos mediante EstanciasViewModel, que recibe datos a tiempo real

    Además, utilizamos varios métodos, indicados al final de la clase
    */

    private lateinit var viewModel: EstanciasViewModel


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
        val imgBebedero = view.findViewById<ImageButton>(R.id.img_bebedero)
        val txtEstadoBebedero = view.findViewById<TextView>(R.id.txt_estadoBebedero)
        val txtRevisionBebedero = view.findViewById<TextView>(R.id.txt_revisionBebedero)
        val txtUltimoUsuarioBebedero = view.findViewById<TextView>(R.id.txt_ultimoUsuarioBebedero)

        // comedero
        val imgComedero = view.findViewById<ImageButton>(R.id.img_comedero)
        val txtEstadoComedero = view.findViewById<TextView>(R.id.txt_estadoComedero)
        val txtRevisionComedero = view.findViewById<TextView>(R.id.txt_revisionComedero)
        val txtUltimoUsuarioComedero = view.findViewById<TextView>(R.id.txt_ultimoUsuarioComedero)

        // gallinero
        val imgGallinero = view.findViewById<ImageButton>(R.id.img_gallinero)
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
                txtEstadoBebedero.setTextColor(obtenerColorDesdeEstado(estado)) // para controlar el color
                txtRevisionBebedero.text = "Última revisión: ${convertirTimestamp(it.timestamp_ultima_revision)}"
                txtUltimoUsuarioBebedero.text = "Revisado por: ${it.ultimo_usuario}"
            }

            comedero?.let {
                val estado = calcularEstadoEstancia(it.timestamp_ultima_revision, it.recurrencia_revision, it.tipo)
                txtEstadoComedero.text = "Estado: $estado"
                txtEstadoComedero.setTextColor(obtenerColorDesdeEstado(estado))// para controlar el color
                txtRevisionComedero.text = "Última revisión: ${convertirTimestamp(it.timestamp_ultima_revision)}"
                txtUltimoUsuarioComedero.text = "Revisado por: ${it.ultimo_usuario}"
            }

            gallinero?.let {
                val estado = calcularEstadoEstancia(it.timestamp_ultima_revision, it.recurrencia_revision, it.tipo)
                txtEstadoGallinero.text = "Estado: $estado"
                txtEstadoGallinero.setTextColor(obtenerColorDesdeEstado(estado)) //para controlar el colr
                txtRevisionGallinero.text = "Última revisión: ${convertirTimestamp(it.timestamp_ultima_revision)}"
                txtUltimoUsuarioGallinero.text = "Revisado por: ${it.ultimo_usuario}"
            }
        }

        // observador del toast
        viewModel.mensajeError.observe(viewLifecycleOwner) { mensaje ->
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
        }

        // ---- lógica UI (listeners, etc)

        // botón bebedero
        imgBebedero.setOnClickListener{

            mostrarDialogRevisionEstancia("bebedero", 10)
        }

        // botón comedero
        imgComedero.setOnClickListener{

            mostrarDialogRevisionEstancia("comedero", 10)
        }

        // botón bebedero
        imgGallinero.setOnClickListener{

            mostrarDialogRevisionEstancia("gallinero", 50)
        }

    }

    // ---- otros métodos

    /*
    -- convertirTimestamp: metodo para convertir un numero timestamp en formato milisegundos a
    una fecha legible por el usuario. Usamos timestamp para poder mostrar la fecha de la ultima
    revisión de la estancia
     */
    private fun convertirTimestamp(timestamp: Long): String {
        return try {
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timestampFecha = Date(timestamp)
            formatoFecha.format(timestampFecha)
        } catch (e: Exception) {
            "Fecha no válida"
        }
    }

    /*
    -- calcularEstadoEstancia: calcula y devuelve el estado actial de una estancia según cuantos
    días han pasado desde su última revisión. En la base de datos cada estancia indica cuántos
    días tarda en tener que ser revisada (recurrencia_revision) y con ello se hace el cálculo
     */
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

    /*
    -- mostrarDialogRevisionEstancia: para indicar al usuario que acaba de revisar una estancia
    de forma divertida
     */
    private fun mostrarDialogRevisionEstancia(
        tipoEstancia: String,
        puntos: Int
    ) {
        val context = requireContext()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_registro_estancia, null)

        // referencias firebase
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firebaseDB = FirebaseDatabase.getInstance().reference
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
                    Utils.mostrarDialogoInformativo(
                        requireContext(),
                        "¡¡Acabas de revisar el $tipoEstancia!! Las gallinas son un poco más felices ahora :)",
                        "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcarita_corazones.gif?alt=media&token=9d32281e-4b4e-4ed9-bcd8-cb963326731a"
                    )
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
    /*
    -- obtenerColorDesdeEstado: para cambiar el color del "estado estancia" dependiendo de si está
    recién revisado, si está intermedoio o si necesita revisión
     */
    private fun obtenerColorDesdeEstado(estado: String): Int {
        return when (estado.lowercase()) {
            "lleno", "limpio" -> requireContext().getColor(R.color.verde)
            "medio", "decente" -> requireContext().getColor(R.color.naranja)
            "vacío", "sucio" -> requireContext().getColor(R.color.rojo)
            else -> requireContext().getColor(android.R.color.black)
        }
    }



}