package theteachercelia.eggstatv1.ui.huevos

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.bd.Gallina
import theteachercelia.eggstatv1.utils.Utils

class HuevosFragment : Fragment() {

    /*
    Fragment que incluye un ImageView (animada con ObjectAnimator) clicable para registrar un huevo
    a la gallina seleccionada. Al hacer clic sobre el huevo:
    * Hace una animación de giro
    * Aparece un Dialog con un Spinner que contiene todas las gallinas
    * Al confirmar, se suma 1 huevo a esa gallina y 5 puntos al usuario autenticado
    * Además, aparece un dialog divertido confirmando, y si no hubiera gallinas, aparece un mensaje
    informativo.
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_huevos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //--- referencias a views o base de datos
        val imgHuevo = view.findViewById<ImageView>(R.id.img_huevo)

        // bd
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseDatabase = FirebaseDatabase.getInstance().reference
        val usuarioID = firebaseAuth.currentUser?.uid ?:""

        //---logica UI (listeners, metodos, etc)

        // animación efecto latido (solo en el eje Y)
        val animacionEjeY = ObjectAnimator.ofFloat(imgHuevo, "scaleY", 1f, 1.1f, 1f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        animacionEjeY.start()

        //al hacer clic sobre la imagen del huevo, se registra un huevo a la gallina y se suma un punto al usuario registrado
        imgHuevo.setOnClickListener {
            // animacion clic (giro huevo)
            val rotate = ObjectAnimator.ofFloat(imgHuevo, "rotation", 0f, 360f).apply {
                duration = 500
            }
            val scaleUp = ObjectAnimator.ofFloat(imgHuevo, "scaleX", 1f, 1.4f, 1f).apply {
                duration = 500
            }

            val scaleUpY = ObjectAnimator.ofFloat(imgHuevo, "scaleY", 1f, 1.4f, 1f).apply {
                duration = 500
            }

            rotate.start()
            scaleUp.start()
            scaleUpY.start()

            // funcionalidad clic
            val context = requireContext()

            // identificamos las partes del dialog_agregar_gallina.xml dentro del dialog para evitar crash
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_registrar_huevo,null)
            val spinnerElegirGallina = dialogView.findViewById<Spinner>(R.id.spinner_huevo)

            // creamos el spinner antes de abrir el dialog
            val gallinasBD = firebaseDatabase.child("gallinas") //buscamos en el nodo gallinas
            val listaGallinas = mutableListOf("Selecciona una gallina")
            val mapaGallinas = mutableMapOf<String, String>()

            gallinasBD.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (gallinaSnap in snapshot.children) {
                        val gallina = gallinaSnap.getValue(Gallina::class.java)
                        val nombre = gallina?.nombre_gallina
                        val key = gallinaSnap.key
                        if (!nombre.isNullOrEmpty() && !key.isNullOrEmpty()) {
                            listaGallinas.add(nombre)
                            mapaGallinas[nombre] = key
                        }
                    }
                    // adapter del spinner
                    val adapter = ArrayAdapter(context,android.R.layout.simple_spinner_item, listaGallinas)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerElegirGallina.adapter = adapter

                    // alertdialog
                    AlertDialog.Builder(context)
                        .setTitle("Registrar huevo")
                        .setView(dialogView)
                        .setPositiveButton("Registrar") { _, _ ->
                            val gallinaSeleccionada = spinnerElegirGallina.selectedItem?.toString() ?: ""
                            val gallinaKey = mapaGallinas[gallinaSeleccionada]

                            // se valida que la opción del spinner no sea "selecciona una gallina"
                            if (gallinaSeleccionada == "Selecciona una gallina") {
                                Toast.makeText(requireContext(), "¡¡Se te olvidó seleccionar una gallina!!", Toast.LENGTH_LONG).show()
                                return@setPositiveButton
                            }

                            if(gallinaKey != null){
                                // sumar 1 huevo a la gallina seleccionada
                                val gallinaRef = gallinasBD.child(gallinaKey)
                                gallinaRef.child("total_huevos").get().addOnSuccessListener { huevosSnap ->
                                    val huevosActuales = huevosSnap.getValue(Int::class.java) ?: 0
                                    gallinaRef.child("total_huevos").setValue(huevosActuales + 1) // se suma 1 huevo a la base de datos de la gallina

                                }

                                // sumar 5 puntos al usuario con el metodo sumarpuntos
                                Utils.sumarPuntos(usuarioID, 5, firebaseDatabase)

                                Utils.mostrarDialogoInformativo(
                                    requireContext(),
                                    "¡¡Acabas de registrar un huevo de $gallinaSeleccionada!! ¿Cómo va en las EggStadísticas?",
                                    "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fparty.gif?alt=media&token=754a138f-51e5-4cdd-88fd-c8decf7fbfd7"
                                )
                            }
                            else{
                                Utils.mostrarDialogoInformativo(
                                    requireContext(),
                                    "Oopsie woopsie... Error al obtener la gallina seleccionada :(",
                                    "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                                )

                            }

                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                else{
                    Utils.mostrarDialogoInformativo(
                        requireContext(),
                        "Oopsie woopsie... ¡No hay gallinas registradas aún! :(",
                        "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                    )
                }
            }

        }
    }

}