package theteachercelia.eggstatv1.ui.huevos

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.bd.Gallina

class HuevosFragment : Fragment() {

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
        //identificamos las partes del dialog_agregar_gallina.xml
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_registrar_huevo,null)
        val spinnerElegirGallina = dialogView.findViewById<Spinner>(R.id.spinner_huevo)

        //bd
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseDatabase = FirebaseDatabase.getInstance().reference
        val usuarioID = firebaseAuth.currentUser?.uid ?:""

        //---logica UI (listeners, metodos, etc)

        //al hacer clic sobre la imagen del huevo, se registra un huevo a la gallina y se suma un punto al usuario registrado
        imgHuevo.setOnClickListener {
            val context = requireContext()

            //creamos el spinner antes de abrir el dialog
            val gallinasBD = firebaseDatabase.child("gallinas") //buscamos en la tabla gallinas
            val usuarioBD = firebaseDatabase.child("usuarios").child(usuarioID)
            val listaGallinas = mutableListOf("Selecciona una gallina")
            val mapaGallinas = mutableMapOf<String, String>()

            //TODO: IMPLEMENTAR AQUI EL METODO SUMAR PUNTOS
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
                    val adapter = ArrayAdapter(context,android.R.layout.simple_spinner_item, listaGallinas)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerElegirGallina.adapter = adapter

                    //alertdialog
                    AlertDialog.Builder(context)
                        .setTitle("Registrar huevo")
                        .setView(dialogView)
                        .setPositiveButton("Registrar") { _, _ ->
                            val gallinaSeleccionada = spinnerElegirGallina.selectedItem?.toString() ?: ""
                            val gallinaKey = mapaGallinas[gallinaSeleccionada]


                            if(gallinaKey != null){
                                // sumar 1 huevo a la gallina seleccionada
                                val gallinaRef = gallinasBD.child(gallinaKey)
                                gallinaRef.child("total_huevos").get().addOnSuccessListener { huevosSnap ->
                                    val huevosActuales = huevosSnap.getValue(Int::class.java) ?: 0
                                    gallinaRef.child("total_huevos").setValue(huevosActuales + 1) // se suma 1 huevo a la base de datos de la gallina

                                }

                                // sumar 5 puntos al usuario conectado

                                usuarioBD.child("puntos_usuario").get().addOnSuccessListener { puntosSnap ->
                                    val ptsUsuarioActuales = puntosSnap.getValue(Int::class.java) ?: 0
                                    usuarioBD.child("puntos_usuario").setValue(ptsUsuarioActuales + 5) // se suman 5 puntos al usuario por registrar el huevo
                                }

                                // sumar 5 puntos al equipo del usuario conectado
                                //1- accedemos al id del equipo_id del usuario
                                usuarioBD.child("equipo_id").get().addOnSuccessListener { equipoSnap ->
                                    //2- añadimos los 5 puntos al equipo
                                    val equipoID = equipoSnap.getValue(String::class.java)
                                    if (!equipoID.isNullOrEmpty()) {
                                        val equipoNormalizado = equipoID.lowercase().replace("\\s+".toRegex(), "")
                                        val equipoRef = firebaseDatabase.child("equipo").child(equipoNormalizado)

                                        equipoRef.child("puntos_equipo").get().addOnSuccessListener { puntosEquipoSnap ->
                                            val puntosEquipoActuales = puntosEquipoSnap.getValue(Int::class.java) ?: 0
                                            equipoRef.child("puntos_equipo").setValue(puntosEquipoActuales + 5)
                                        }
                                    }
                                }

                                Toast.makeText(context, "¡Huevo registrado con éxito!", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                Toast.makeText(context, "Error al obtener la gallina seleccionada", Toast.LENGTH_SHORT).show()
                            }

                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                else{
                    Toast.makeText(context, "No hay gallinas registradas aún", Toast.LENGTH_SHORT).show()
                }
            }



        }
    }


}