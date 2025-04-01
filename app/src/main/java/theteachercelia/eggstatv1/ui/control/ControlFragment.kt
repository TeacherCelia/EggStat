package theteachercelia.eggstatv1.ui.control

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import theteachercelia.eggstatv1.R
import android.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import theteachercelia.eggstatv1.bd.Equipo
import theteachercelia.eggstatv1.bd.Usuario

class ControlFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // "inflamos" la vista del fragment
        val view = inflater.inflate(R.layout.fragment_control, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // instanciamos firebase auth y firebase database
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference

        /********************
        boton agregar usuario
         ********************/

        view.findViewById<Button>(R.id.btnAgregarUsuario).setOnClickListener {
            // usamos un dialogview para agregar usuarios
            val context = requireContext()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_crear_usuario, null)

            // identificar las partes del dialog con los ids del dialog_crear_usuario.xml
            val inputUsuario = dialogView.findViewById<EditText>(R.id.edtxt_CrearUsuario)
            val inputPass = dialogView.findViewById<EditText>(R.id.edtxt_CrearContrasena)
            val inputRepetirPass = dialogView.findViewById<EditText>(R.id.edtxt_RepetirContrasena)
            val spinnerEquipos = dialogView.findViewById<Spinner>(R.id.spinner_Equipo)

            val equiposRef = database.child("equipo")
            val listaEquipos = mutableListOf("Selecciona un equipo") //***

            // cargar los equipos MENOS profesores
            equiposRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()){ //para añadir a "selecciona un equipo" el resto de equipos que hay en firebase "equipo"
                    for (equipoSnap in snapshot.children) {
                        val nombreEquipo = equipoSnap.child("nombre_equipo").getValue(String::class.java)
                        if (!nombreEquipo.isNullOrEmpty() && nombreEquipo.lowercase() != "profesores") { // no añadimos el equipo profesores
                            listaEquipos.add(nombreEquipo)
                        }
                    }
                    // Adaptador y asignar al Spinner
                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listaEquipos)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerEquipos.adapter = adapter
                }

                //alertdialog con las opciones para introducir el alumno
                AlertDialog.Builder(context)
                    .setTitle("Crear nuevo usuario")
                    .setView(dialogView)
                    .setPositiveButton("Crear") { _, _ ->
                        val usuario = inputUsuario.text.toString().trim()
                        val password = inputPass.text.toString()
                        val repetirPassword = inputRepetirPass.text.toString()
                        val equipoSeleccionado = spinnerEquipos.selectedItem?.toString() ?: ""

                        if (usuario.isEmpty() || password.isEmpty() || equipoSeleccionado == "Selecciona un equipo") {
                            Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        if (password != repetirPassword) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        val emailFake = "$usuario@eggstat.com"

                        auth.createUserWithEmailAndPassword(emailFake, password)
                            .addOnSuccessListener { result ->
                                val uid = result.user?.uid ?: return@addOnSuccessListener
                                // creamos un nuevo objeto usuario con esos atributos
                                val nuevoUsuario = Usuario(
                                    nombre_usuario = usuario,
                                    rol = "alumno",
                                    equipo_id = equipoSeleccionado,
                                    puntos_usuario = 0,
                                    email = emailFake
                                )

                                database.child("usuarios").child(uid).setValue(nuevoUsuario)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Usuario creado correctamente", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error guardando en BD", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error creando usuario: ${it.message}", Toast.LENGTH_LONG).show()
                            }

                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        /******************
        boton agregar equipo
         ******************/

        view.findViewById<Button>(R.id.btnAgregarEquipo).setOnClickListener {
            val context = requireContext()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_crear_equipo, null)

            //identificamos las partes del dialog con los id del dialog_crear_equipo
            val inputNombreEquipo = dialogView.findViewById<EditText>(R.id.edtxt_nombreEquipo)

            //alertdialog
            AlertDialog.Builder(context)
                .setTitle("Crear un nuevo equipo")
                .setView(dialogView)
                .setPositiveButton("Crear") { _, _ ->
                    val equipo = inputNombreEquipo.text.toString().trim()

                    // si el campo está vacío
                    if (equipo.isEmpty()) {
                        Toast.makeText(context, "¡¡Ponle nombre al equipo!!", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // funcionalidad para no repetir equipos
                    val nombreEquipoMinus = equipo.lowercase().replace("\\s+".toRegex(), "")

                    val equipoRef = database.child("equipo")

                    // si existe, salta el TOAST, si no existe, se crea
                    equipoRef.child(nombreEquipoMinus).get().addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            Toast.makeText(context, "Ese equipo ya existe :(", Toast.LENGTH_SHORT).show()
                        } else {
                            val nuevoEquipo = Equipo(
                                nombre_equipo = equipo,
                                puntos_equipo = 0
                            )
                            equipoRef.child(nombreEquipoMinus).setValue(nuevoEquipo)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "¡¡Nuevo equipo creado!!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al guardar el equipo", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }

                }
                .setNegativeButton("Cancelar", null)
                .show()

        }

        /********************
        boton agregar gallina
         ********************/
        // TODO: implementar funcionalidad boton agregar gallina
        view.findViewById<Button>(R.id.btnAgregarGallina).setOnClickListener {
            Toast.makeText(requireContext(), "Aquí se añadirá una gallina", Toast.LENGTH_SHORT).show()
        }


    }

}