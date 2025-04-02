package theteachercelia.eggstatv1.ui.control

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import theteachercelia.eggstatv1.R
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import theteachercelia.eggstatv1.bd.Equipo
import theteachercelia.eggstatv1.bd.Gallina
import theteachercelia.eggstatv1.bd.Usuario

class ControlFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // "inflamos" la vista del fragment
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    // para usar activityResultLauncher (imagenes)
    private lateinit var seleccionarImagenLauncher: ActivityResultLauncher<Intent>
    private var imagenUriSeleccionada: Uri?= null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // instanciamos firebase auth, firebase database y firebase storage
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseDatabase = FirebaseDatabase.getInstance().reference
        val firebaseStorage = FirebaseStorage.getInstance().reference

        // registramos el launcher de imagenes
        seleccionarImagenLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                imagenUriSeleccionada = result.data?.data
                Toast.makeText(context, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }

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

            val equiposRef = firebaseDatabase.child("equipo")
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

                    // para resetear imagen después de guardar
                    imagenUriSeleccionada = null

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

                        firebaseAuth.createUserWithEmailAndPassword(emailFake, password)
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

                                firebaseDatabase.child("usuarios").child(uid).setValue(nuevoUsuario)
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

                    val equipoRef = firebaseDatabase.child("equipo")

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

        view.findViewById<Button>(R.id.btnAgregarGallina).setOnClickListener {
            // creamos la view con el dialogview
            val context = requireContext()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_agregar_gallina, null)

            // identificamos todos los componentes
            val inputNombre = dialogView.findViewById<EditText>(R.id.edtxt_nombreGallina)
            val inputRaza = dialogView.findViewById<EditText>(R.id.edtxt_razaGallina)
            val inputEdad = dialogView.findViewById<EditText>(R.id.edtxt_edadGallina)
            val inputTotalHuevos = dialogView.findViewById<EditText>(R.id.edtxt_totalHuevos)
            val btnSeleccionarFoto = dialogView.findViewById<Button>(R.id.btn_seleccionarFoto)

            /********************************
            boton seleccionar foto de gallina
             ********************************/


            btnSeleccionarFoto.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                seleccionarImagenLauncher.launch(intent)
            }

            AlertDialog.Builder(context)
                .setTitle("Registrar gallina")
                .setView(dialogView)
                .setPositiveButton("Crear") { _, _ ->
                    val nombreGallina = inputNombre.text.toString().trim()
                    val raza = inputRaza.text.toString().trim()
                    val edadGallina = inputEdad.text.toString().trim()
                    val huevosGallina = inputTotalHuevos.text.toString().trim()

                    if (nombreGallina.isEmpty() || raza.isEmpty() || edadGallina.isEmpty() || huevosGallina.isEmpty()) {
                        Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // pasamos los strings de los inputs a int
                    val edad = edadGallina.toIntOrNull()
                    val huevos = huevosGallina.toIntOrNull()

                    if (edad == null || huevos == null) {
                        Toast.makeText(context, "¡¡Edad y huevos deben ser números!!", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (imagenUriSeleccionada == null) {
                        Toast.makeText(context, "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // ---SUbir imagen a firebase storage
                    val nombreArchivo = "gallinas/${System.currentTimeMillis()}_${nombreGallina}.jpg"
                    val imagenRef = FirebaseStorage.getInstance().reference.child(nombreArchivo)

                    imagenUriSeleccionada?.let { uri ->
                        imagenRef.putFile(uri)
                            .addOnSuccessListener {
                                // obtener la URL de descarga
                                imagenRef.downloadUrl.addOnSuccessListener { url ->
                                    val nuevaGallina = Gallina(
                                        nombre_gallina = nombreGallina,
                                        raza = raza,
                                        edad = edad,
                                        total_huevos = huevos,
                                        foto_url = url.toString()
                                    )

                                    // Guardamos la gallina en Realtime Database
                                    val gallinasRef = FirebaseDatabase.getInstance().reference.child("gallinas")
                                    gallinasRef.push().setValue(nuevaGallina)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "¡¡Gallina añadida con éxito!!", Toast.LENGTH_LONG).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Error al guardar la gallina", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                            }
                    }

                }
                .setNegativeButton("Cancelar", null)
                .show()


        }


    }

}