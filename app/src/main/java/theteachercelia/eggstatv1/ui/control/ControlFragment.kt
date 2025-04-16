package theteachercelia.eggstatv1.ui.control

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.bd.Equipo
import theteachercelia.eggstatv1.bd.Gallina
import theteachercelia.eggstatv1.bd.Usuario
import theteachercelia.eggstatv1.utils.Utils

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

        // referencias a lasviews de los botones
        val btnAgregarUsuario = view.findViewById<Button>(R.id.btn_AgregarUsuario)
        val btnAgregarEquipo = view.findViewById<Button>(R.id.btn_AgregarEquipo)
        val btnAgregarGallina = view.findViewById<Button>(R.id.btn_AgregarGallina)
        val btnCanjearPtsEquipo = view.findViewById<Button>(R.id.btn_canjearPtsEquipo)
        val btnCanjearPtsUsuario = view.findViewById<Button>(R.id.btn_canjearPtsUsuario)

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

        btnAgregarUsuario.setOnClickListener {
            // usamos un dialogview para agregar usuarios
            val context = requireContext()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_crear_usuario, null)

            // identificar las partes del dialog con los ids del dialog_crear_usuario.xml
            val inputUsuario = dialogView.findViewById<EditText>(R.id.edtxt_CrearUsuario)
            val inputPass = dialogView.findViewById<EditText>(R.id.edtxt_CrearContrasena)
            val inputRepetirPass = dialogView.findViewById<EditText>(R.id.edtxt_RepetirContrasena)
            val spinnerEquipos = dialogView.findViewById<Spinner>(R.id.spinner_Equipo)

            val equiposRef = firebaseDatabase.child("equipo")
            val listaEquipos = mutableListOf("Selecciona un equipo")

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

                        //creo un email con el nombre de usuario para que Firebase me acepte el usuario
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

        btnAgregarEquipo.setOnClickListener {
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

        btnAgregarGallina.setOnClickListener {
            // creamos la view con el dialogview
            val context = requireContext()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_agregar_gallina, null)

            // identificamos todos los componentes
            val inputNombre = dialogView.findViewById<EditText>(R.id.edtxt_nombreGallina)
            val inputRaza = dialogView.findViewById<EditText>(R.id.edtxt_razaGallina)
            val inputFechaNacimiento = dialogView.findViewById<DatePicker>(R.id.dp_edadGallina)
            val inputTotalHuevos = dialogView.findViewById<EditText>(R.id.edtxt_totalHuevos)
            val btnSeleccionarFoto = dialogView.findViewById<Button>(R.id.btn_seleccionarFoto)

            //evitar poder seleccionar fechas futuras
            inputFechaNacimiento.maxDate = System.currentTimeMillis()

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
                    val huevosGallina = inputTotalHuevos.text.toString().trim()
                    //referencias al datepicker
                    val dia = inputFechaNacimiento.dayOfMonth
                    val mes = inputFechaNacimiento.month +1
                    val anio = inputFechaNacimiento.year
                    val fechaNacimiento = String.format("%04d-%02d-%02d", anio, mes, dia)

                    if (nombreGallina.isEmpty() || raza.isEmpty() || huevosGallina.isEmpty()) {
                        Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // pasamos los strings de los inputs a int
                    val huevos = huevosGallina.toIntOrNull()
                    if (huevos == null) {
                        Toast.makeText(context, "¡¡Los huevos deben ser un número!!", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    if (imagenUriSeleccionada == null) {
                        Toast.makeText(context, "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // ---SUbir imagen a firebase storage
                    val nombreArchivo = "gallinas/${System.currentTimeMillis()}_${nombreGallina}.jpg"
                    val imagenGallina = firebaseStorage.child(nombreArchivo)

                    imagenUriSeleccionada?.let { uri ->
                        imagenGallina.putFile(uri)
                            .addOnSuccessListener {
                                // obtener la URL de descarga
                                imagenGallina.downloadUrl.addOnSuccessListener { url ->
                                    val nuevaGallina = Gallina(
                                        nombre_gallina = nombreGallina,
                                        raza = raza,
                                        fecha_nacimiento = fechaNacimiento,
                                        total_huevos = huevos,
                                        foto_url = url.toString()
                                    )

                                    // Guardamos la gallina en Realtime Database
                                    val gallinaGuardada = firebaseDatabase.child("gallinas")
                                    gallinaGuardada.push().setValue(nuevaGallina)
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

        /**************************
        boton canjear puntos equipo
         **************************/

        btnCanjearPtsEquipo.setOnClickListener{
            val context = requireContext()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_canjear_pts_equipo, null)

            val spinner = dialogView.findViewById<Spinner>(R.id.spinner_PtsEquipo)
            val edtxtPuntos = dialogView.findViewById<EditText>(R.id.edtxt_ptsCanjearEquipo)
            val txtPuntosEquipoActuales = dialogView.findViewById<TextView>(R.id.txt_puntosEquipoActuales)

            val listaEquipos = mutableListOf("Selecciona un equipo") // para que esta sea la primera opcion
            val firebaseDB = FirebaseDatabase.getInstance().reference

            /*
             Para añadir todos los datos al spinner, tenemos que obtener los equipos, hacer un adapter
             para que se puedan visualizar los datos, y mostrar los puntos en el textview.
             */
            // 1. Obtener todos los equipos
            firebaseDB.child("equipo").get().addOnSuccessListener { snapshot ->
                for (equipoSnap in snapshot.children) {
                    equipoSnap.key?.let { listaEquipos.add(it) }
                }

                // 2. Adapter para el Spinner
                val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listaEquipos)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                // 3. Mostrar puntos en el textview al cambiar seleccion del equipo en el spinner
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { // cuando el usuario selecciona una opción
                        val equipoSeleccionado = listaEquipos[position] // equipo elegido segun el atributo "position"

                        //referencia de equipo a firebase
                        val refEquipo = firebaseDB.child("equipo").child(equipoSeleccionado)
                        //lectura de puntos
                        refEquipo.child("puntos_equipo").get().addOnSuccessListener { puntosSnap ->
                            val puntos = puntosSnap.getValue(Int::class.java) ?: 0
                            txtPuntosEquipoActuales.text = "Puntos actuales: $puntos"
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // 4. Dialog para canjear
                AlertDialog.Builder(context)
                    .setTitle("Canjear puntos del equipo")
                    .setView(dialogView)
                    .setPositiveButton("Canjear") { _, _ ->
                        val equipoSeleccionado = spinner.selectedItem?.toString()
                        val puntosACanjear = edtxtPuntos.text.toString().toIntOrNull()

                        if (equipoSeleccionado == "Selecciona un equipo" || puntosACanjear == null || puntosACanjear <= 0) {
                            Toast.makeText(context, "¡¡Selecciona equipo y puntos válidos!!", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        val equipoRef = firebaseDB.child("equipo").child(equipoSeleccionado!!) //pongo !! porque no será null
                        equipoRef.child("puntos_equipo").get().addOnSuccessListener { snap ->
                            val puntosActuales = snap.getValue(Int::class.java) ?: 0

                            if (puntosACanjear > puntosActuales) {
                                Toast.makeText(context, "¡¡El equipo no tiene tantos puntos!!", Toast.LENGTH_SHORT).show()
                            } else {
                                // llamamos al metodo de restar puntos en Utils
                                Utils.restarPuntos(equipoSeleccionado, puntosACanjear, firebaseDB, "equipo")
                                Toast.makeText(context, "Puntos canjeados correctamente", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        /***************************
        boton canjear puntos usuario
         ***************************/

        btnCanjearPtsUsuario.setOnClickListener {
            val context = requireContext()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_canjear_pts_usuario, null)

            val spinner = dialogView.findViewById<Spinner>(R.id.spinner_PtsUsuario)
            val edtxtPuntos = dialogView.findViewById<EditText>(R.id.edtxt_ptsCanjearUsuario)
            val txtPuntosUsuarioActuales = dialogView.findViewById<TextView>(R.id.txt_puntosUsuarioActuales)

            val mapaUsuarios = mutableMapOf<String, String>() // mapa nombre_usuario - uid
            val listaNombres = mutableListOf("Selecciona un alumno")
            val firebaseDB = FirebaseDatabase.getInstance().reference

            // obtenemos usuarios de Firebase
            firebaseDB.child("usuarios").get().addOnSuccessListener { snapshot ->
                for (usuarioSnap in snapshot.children) {
                    val uid = usuarioSnap.key
                    val nombre = usuarioSnap.child("nombre_usuario").getValue(String::class.java)

                    if (!uid.isNullOrEmpty() && !nombre.isNullOrEmpty()) {
                        listaNombres.add(nombre)
                        mapaUsuarios[nombre] = uid
                    }
                }

                // adapter del spinner
                val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listaNombres)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                // mostrar puntos cuando se selecciona un usuario en el TextView
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val nombreSeleccionado = listaNombres[position]
                        val uidSeleccionado = mapaUsuarios[nombreSeleccionado]

                        if (uidSeleccionado != null) {
                            val refUsuario = firebaseDB.child("usuarios").child(uidSeleccionado)
                            refUsuario.child("puntos_usuario").get().addOnSuccessListener { puntosSnap ->
                                val puntos = puntosSnap.getValue(Int::class.java) ?: 0
                                txtPuntosUsuarioActuales.text = "Puntos actuales: $puntos"
                            }
                        } else {
                            txtPuntosUsuarioActuales.text = ""
                        }
                    }

                    //funcion obligatoria que dejamos vacia, siendo <*> cualquier tipo de AdapterView
                    override fun onNothingSelected(spinner: AdapterView<*>?) {}
                }

                // se crea el alertdialog
                AlertDialog.Builder(context)
                    .setTitle("Canjear puntos del usuario")
                    .setView(dialogView)
                    .setPositiveButton("Canjear") { _, _ ->
                        val nombreSeleccionado = spinner.selectedItem?.toString()
                        val puntosACanjear = edtxtPuntos.text.toString().toIntOrNull()

                        if (nombreSeleccionado == "Selecciona un usuario" || puntosACanjear == null || puntosACanjear <= 0) {
                            Toast.makeText(context, "¡¡Selecciona usuario y puntos válidos!!", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        val uidSeleccionado = mapaUsuarios[nombreSeleccionado]
                        if (uidSeleccionado == null) {
                            Toast.makeText(context, "Error interno: no se encontró UID", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        val usuarioRef = firebaseDB.child("usuarios").child(uidSeleccionado)
                        usuarioRef.child("puntos_usuario").get().addOnSuccessListener { snap ->
                            val puntosActuales = snap.getValue(Int::class.java) ?: 0

                            if (puntosACanjear > puntosActuales) {
                                Toast.makeText(context, "¡¡El usuario no tiene tantos puntos!!", Toast.LENGTH_SHORT).show()
                            } else {
                                Utils.restarPuntos(uidSeleccionado, puntosACanjear, firebaseDB, "usuario")
                                Toast.makeText(context, "¡¡Puntos canjeados correctamente!!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

    }

}