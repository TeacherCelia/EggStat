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
import android.widget.ImageButton
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

    /*
    Fragment donde realizar las principales acciones de control de la app:
    - Añadir usuarios
    - Añadir gallinas
    - Añadir equipos
    - Canjear puntos de usuarios
    - Canjear puntos de equipo
    - Resetear huevos
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // "inflamos" la vista del fragment desde el xml
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    // para usar activityResultLauncher al subir una imagen de una gallina
    private lateinit var seleccionarImagenLauncher: ActivityResultLauncher<Intent>
    private var imagenUriSeleccionada: Uri?= null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // definimos la variable context que usaremos en todos los botones
        val context = requireContext()

        // instanciamos firebase auth, firebase database y firebase storage
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseDB = FirebaseDatabase.getInstance().reference
        val firebaseStorage = FirebaseStorage.getInstance().reference

        // referencias a lasviews de los botones
        val btnAgregarUsuario = view.findViewById<ImageButton>(R.id.btn_AgregarUsuario)
        val btnAgregarEquipo = view.findViewById<ImageButton>(R.id.btn_AgregarEquipo)
        val btnAgregarGallina = view.findViewById<ImageButton>(R.id.btn_AgregarGallina)
        val btnCanjearPtsEquipo = view.findViewById<Button>(R.id.btn_canjearPtsEquipo)
        val btnCanjearPtsUsuario = view.findViewById<Button>(R.id.btn_canjearPtsUsuario)
        val btnResetearHuevos = view.findViewById<Button>(R.id.btn_resetearHuevos)

        // registramos el launcher para subir imagenes
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

        // --------------------------------- //
        // ----- BOTON AGREGAR USUARIO ----- //
        // --------------------------------- //

        //- se abre dialog para indicar el nombre de usuario la contraseña (repetida) y seleccionar el equipo del usuario
        //- se hacen las comprobaciones de que todos los campos estén rellenos y de que las contraseñas coincidan
        //- finalmente, se añade el usuario con estos datos a Firebase Database

        btnAgregarUsuario.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_crear_usuario, null)

            // identificamos las partes del dialog con los ids del dialog_crear_usuario.xml
            val inputUsuario = dialogView.findViewById<EditText>(R.id.edtxt_CrearUsuario)
            val inputPass = dialogView.findViewById<EditText>(R.id.edtxt_CrearContrasena)
            val inputRepetirPass = dialogView.findViewById<EditText>(R.id.edtxt_RepetirContrasena)
            val spinnerEquipos = dialogView.findViewById<Spinner>(R.id.spinner_Equipo)

            // referencia a los equipos de la BD y variable que crea una lista de todos
            val equiposRef = firebaseDB.child("equipo")
            val listaEquipos = mutableListOf("Selecciona un equipo")

            // cargar los equipos MENOS los profesores
            equiposRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()){ //para añadir a "selecciona un equipo" el resto de equipos que hay en firebase "equipo"
                    for (equipoSnap in snapshot.children) {
                        val nombreEquipo = equipoSnap.child("nombre_equipo").getValue(String::class.java)
                        if (!nombreEquipo.isNullOrEmpty() && nombreEquipo.lowercase() != "profesores") { // no añadimos el equipo profesores
                            listaEquipos.add(nombreEquipo)
                        }
                    }
                    // adapter del spinner
                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listaEquipos)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerEquipos.adapter = adapter

                    // para resetear imagen después de guardar
                    imagenUriSeleccionada = null

                }

                // --- alertdialog con las opciones para introducir el alumno ---

                // se crea el dialog de esta forma para controlar el boton "crear"
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Crear nuevo usuario")
                    .setView(dialogView)
                    .setPositiveButton("Crear", null) // null para controlar el boton más adelante
                    .setNegativeButton("Cancelar", null)
                    .create()

                dialog.setOnShowListener {
                    val btnCrear = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    btnCrear.setOnClickListener {
                        val usuario = inputUsuario.text.toString().trim()
                        val password = inputPass.text.toString()
                        val repetirPassword = inputRepetirPass.text.toString()
                        val equipoSeleccionado = spinnerEquipos.selectedItem?.toString() ?: ""

                        // validamos que no coincidan contraseñas y que tdo esté relleno
                        if (usuario.isEmpty() || password.isEmpty() || equipoSeleccionado == "Selecciona un equipo") {
                            Toast.makeText(context, "¡¡Rellena todos los campos!!", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (password != repetirPassword) {
                            Toast.makeText(context, "Las contraseñas no coinciden :(", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // variable para crear un email fake al usuario (requisito de FirebaseAuth)
                        val emailFake = "$usuario@eggstat.com"

                        firebaseAuth.createUserWithEmailAndPassword(emailFake, password)
                            .addOnSuccessListener { result ->
                                val uid = result.user?.uid ?: return@addOnSuccessListener
                                val nuevoUsuario = Usuario(
                                    nombre_usuario = usuario,
                                    rol = "alumno",
                                    equipo_id = equipoSeleccionado,
                                    puntos_usuario = 0,
                                    email = emailFake // aqui seria su email "falso" con el que se registra en FirebaseAuth
                                )

                                // mensajes al acabar el proceso
                                firebaseDB.child("usuarios").child(uid).setValue(nuevoUsuario)
                                    .addOnSuccessListener {
                                        Utils.mostrarDialogoInformativo(
                                            context,
                                            "¡¡Usuario creado!! Infórmale de cuál será su nombre de usuario y contraseña para acceder :)",
                                            "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fparty.gif?alt=media&token=754a138f-51e5-4cdd-88fd-c8decf7fbfd7"
                                        )
                                        dialog.dismiss() // cerramos el diálogo solo si tdo ha salido bien
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error guardando en BD", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Utils.mostrarDialogoInformativo(
                                    context,
                                    "Oopsie woopsie... Ha habido un error creando el usuario: ${it.message}",
                                    "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                                )
                            }
                    }
                }

                dialog.show()

            }
        }


        // --------------------------------- //
        // ----- BOTON AGREGAR GALLINA ----- //
        // --------------------------------- //

        //- se abre dialog para indicar el nombre la raza la edad la cantidad de huevos y una imagen
        //- el dialog cuenta con dos componentes especiales: datepicker (para la fecha) y un botón para subir imagen a FirebaseStorage
        //- al pulsar en "crear" los datos se suben a Firebase Database y a Firebase Storage (en FirebaseDB se guarda la URL de imagen también)

        btnAgregarGallina.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_agregar_gallina, null)

            // identificamos todos los componentes
            val inputNombre = dialogView.findViewById<EditText>(R.id.edtxt_nombreGallina)
            val inputRaza = dialogView.findViewById<EditText>(R.id.edtxt_razaGallina)
            val inputFechaNacimiento = dialogView.findViewById<DatePicker>(R.id.dp_edadGallina)
            val inputTotalHuevos = dialogView.findViewById<EditText>(R.id.edtxt_totalHuevos)
            val btnSeleccionarFoto = dialogView.findViewById<Button>(R.id.btn_seleccionarFoto)

            //evitar poder seleccionar fechas futuras
            inputFechaNacimiento.maxDate = System.currentTimeMillis()

            // ----- BOTON seleccionar foto de gallina ---- //
            btnSeleccionarFoto.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                seleccionarImagenLauncher.launch(intent)
            }

            // dialog para rellenar los datos de la gallina
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

                    // validación de rellenar todos los campos antes de crear una nueva gallina
                    if (nombreGallina.isEmpty() || raza.isEmpty() || huevosGallina.isEmpty()) {
                        Toast.makeText(context, "¡¡Rellena todos los campos!!", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // pasamos los strings de los inputs a int
                    val huevos = huevosGallina.toIntOrNull()
                    if (huevos == null) {
                        Toast.makeText(context, "¡¡Los huevos deben ser un número!!", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    if (imagenUriSeleccionada == null) {
                        Toast.makeText(context, "¡¡Debes seleccionar una imagen!!", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // subir imagen a firebase storage
                    val nombreArchivo = "gallinas/${System.currentTimeMillis()}_${nombreGallina}.jpg"
                    val imagenGallina = firebaseStorage.child(nombreArchivo)

                    // dialog que muestra mensaje de cargando
                    val dialogCargando = Utils.mostrarDialogoCargando(context, "Subiendo gallina...")

                    // proceso de subida de imagen
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

                                    // se guarda la gallina en Firebase Database
                                    val gallinaGuardada = firebaseDB.child("gallinas")
                                    gallinaGuardada.push().setValue(nuevaGallina)
                                        .addOnSuccessListener {
                                            dialogCargando.dismiss() // acaba el dialog cargando
                                            Utils.mostrarDialogoInformativo(
                                                context,
                                                "¡¡Gallina añadida!! Ya aparece en la pantalla Gallinas y en las EggStadísticas ;)",
                                                "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fparty.gif?alt=media&token=754a138f-51e5-4cdd-88fd-c8decf7fbfd7"
                                            )
                                        }
                                        .addOnFailureListener {
                                            dialogCargando.dismiss()
                                            Toast.makeText(context, "Error al guardar la gallina... inténtalo de nuevo", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al subir la imagen... inténtalo de nuevo", Toast.LENGTH_SHORT).show()
                            }
                    }

                }
                .setNegativeButton("Cancelar", null)
                .show()


        }

        // -------------------------------- //
        // ----- BOTON AGREGAR EQUIPO ----- //
        // -------------------------------- //

        /*
        al igual que en los botones anteriores, se mostrará un dialog para introducir los datos del
        equipo nuevo, pero en este caso solo aparecerá un spinner con una lista de nombres pre-cargados
        en un nodo img_equipos. Esto limita los equipos, pero permite que la aplicación muestre una
        imagen divertida del animal del equipo seleccionado. Por ello, el proceso después de abrir el
        dialog es:
        1- cargará nombres del nodo img_equipos
        2- leerá nombres del nodo equipo para saber cuales existen
        3- mostrará en el spinner solo los equipos disponibles para crear
        4- al seleccionar uno y "crearlo", se añade al nodo equipo con sus 3 atributos: nombre, puntos (0) y URL (cogida de img_equipos)
         */

        btnAgregarEquipo.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_crear_equipo, null)

            // tendrá un spinner con los equipos disponibles
            val spinner = dialogView.findViewById<Spinner>(R.id.spinner_EquiposDisponibles)

            // crearemos un mapa de las url y una lista de los equipos disponibles
            val listaDisponibles = mutableListOf<String>()
            val mapaUrls = mutableMapOf<String, String>()

            // referencias a la BD
            val refImagenes = firebaseDB.child("img_equipos")
            val refEquipos = firebaseDB.child("equipo")

            // obtenemos todas las imagenes del catalogo (nodo img_equipos)
            refImagenes.get().addOnSuccessListener { snapshotImagenes ->
                for (imgSnap in snapshotImagenes.children) {
                    val nombre = imgSnap.key ?: continue
                    val url = imgSnap.getValue(String::class.java) ?: continue
                    mapaUrls[nombre] = url
                }

                // obtenemos los equipos existentes para excluirlos
                refEquipos.get().addOnSuccessListener { snapshotEquipos ->
                    val existentes = snapshotEquipos.children.mapNotNull {
                        it.child("nombre_equipo").getValue(String::class.java)?.lowercase()
                    }

                    // se filtran los que no existen todavía
                    listaDisponibles.addAll(mapaUrls.keys.filter { it.lowercase() !in existentes })
                    // se pone de primera opción "selecciona un equipo")
                    listaDisponibles.add(0, "Selecciona un equipo")

                    // controlamos el posible "error" de que todos los equipos ya estén creados
                    if (listaDisponibles.isEmpty()) {
                        Utils.mostrarDialogoInformativo(
                            requireContext(),
                            "Oopsie woopsie... Parece que todos los equipos están ya creados. ¡Dile a la creadora de la app (@theteachercelia) que añada más!",
                            "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                        )
                        return@addOnSuccessListener
                    }

                    // adapter del spinner
                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listaDisponibles)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    // Mostramos el AlertDialog
                    AlertDialog.Builder(context)
                        .setTitle("Crear un nuevo equipo")
                        .setView(dialogView)
                        .setPositiveButton("Crear") { _, _ ->
                            val nombreSeleccionado = spinner.selectedItem?.toString()!! // forzamos que no sea null (no deberia serlo)
                            val url = mapaUrls[nombreSeleccionado]

                            // controlamos posibles errores

                            // si no selecciona un equipo
                            if (nombreSeleccionado == "Selecciona un equipo") {
                                Toast.makeText(context, "¡¡No has seleccionado un equipo!!", Toast.LENGTH_SHORT).show()
                                return@setPositiveButton
                            }

                            // si no se encuentra la imagen (no debería pasar...)
                            if (url.isNullOrEmpty()) {
                                Utils.mostrarDialogoInformativo(
                                    requireContext(),
                                    "Oopsie woopsie... No se ha encontrado una imagen... ¡Habla con @theteachercelia!",
                                    "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                                )
                                return@setPositiveButton
                            }

                            // si tdo va bien, se crea un equipo
                            val equipoNuevo = Equipo(
                                nombre_equipo = nombreSeleccionado,
                                puntos_equipo = 0,
                                url_imagen_equipo = url
                            )

                            // se crea el nodo con el nombre sin espacios
                            val claveEquipo = nombreSeleccionado.lowercase().replace("\\s+".toRegex(), "")
                            refEquipos.child(claveEquipo).setValue(equipoNuevo)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "¡¡Nuevo equipo creado!!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al guardar el equipo", Toast.LENGTH_SHORT).show()
                                }

                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }

        }


        // ---------------------------------------- //
        // ----- BOTON CANJEAR PUNTOS: EQUIPO ----- //
        // ---------------------------------------- //

        /*
        En este dialog se cargará un spinner con todos los equipos disponibles en el nodo equipos,
        un textView con los puntos que ese equipo seleccionado tiene, y un editText para indicar
        los puntos a canjear. La funcionalidad del botón, por tanto, es la siguiente:

        1- Al abrir el dialog, se obtienen todos los equipos y se colocan en el spinner
        2- Al seleccionar un equipo en el spinner, el numero de puntos de ese equipo se muestra en el textview
        3- Al escribir los puntos a canjear, se valida si son más de los que tiene, y si son los
        correctos, se restan a ese equipo de la base de datos llamando al metodo de utils restarpuntos
         */

        btnCanjearPtsEquipo.setOnClickListener{
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_canjear_pts_equipo, null)

            val spinner = dialogView.findViewById<Spinner>(R.id.spinner_PtsEquipo)
            val edtxtPuntos = dialogView.findViewById<EditText>(R.id.edtxt_ptsCanjearEquipo)
            val txtPuntosEquipoActuales = dialogView.findViewById<TextView>(R.id.txt_puntosEquipoActuales)

            val listaEquipos = mutableListOf("Selecciona un equipo") // para que esta sea la primera opcion

            // se obtienen todos los equipos
            firebaseDB.child("equipo").get().addOnSuccessListener { snapshot ->
                for (equipoSnap in snapshot.children) {
                    equipoSnap.key?.let { listaEquipos.add(it) }
                }

                // adapter del spinner
                val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listaEquipos)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                // se muestran los puntos en el textview al cambiar seleccion del equipo en el spinner
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

                    //funcion obligatoria que dejamos vacia, siendo <*> cualquier tipo de AdapterView
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // con todos estos datos, se muestra el dialog para canjear
                AlertDialog.Builder(context)
                    .setTitle("Canjear puntos del equipo")
                    .setView(dialogView)
                    .setPositiveButton("Canjear") { _, _ ->
                        val equipoSeleccionado = spinner.selectedItem?.toString()
                        val puntosACanjear = edtxtPuntos.text.toString().toIntOrNull()

                        // validación de que se haya seleccionado un equipo
                        if (equipoSeleccionado == "Selecciona un equipo" || puntosACanjear == null || puntosACanjear <= 0) {
                            Toast.makeText(context, "¡¡Selecciona equipo y puntos válidos!!", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        // operación de canje de puntos en la BD
                        val equipoRef = firebaseDB.child("equipo").child(equipoSeleccionado!!) //pongo !! porque no será null
                        equipoRef.child("puntos_equipo").get().addOnSuccessListener { snap ->
                            val puntosActuales = snap.getValue(Int::class.java) ?: 0

                            // si son más de los puntos actuales, no se hace nada, solo se muestra Toast
                            if (puntosACanjear > puntosActuales) {
                                Toast.makeText(context, "¡¡El equipo no tiene tantos puntos!!", Toast.LENGTH_SHORT).show()
                            } else {
                                // si son los correctos, llamamos al metodo de restar puntos en Utils
                                Utils.restarPuntos(equipoSeleccionado, puntosACanjear, firebaseDB, "equipo")
                                Toast.makeText(context, "Puntos canjeados correctamente", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        // ---------------------------------------- //
        // ----- BOTON CANJEAR PUNTOS: USUARIO ---- //
        // ---------------------------------------- //

        /*
        Este botón funciona de la misma manera que el botón anterior, cambiando el nodo usuarios por el de equipos
         */

        btnCanjearPtsUsuario.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_canjear_pts_usuario, null)

            val spinner = dialogView.findViewById<Spinner>(R.id.spinner_PtsUsuario)
            val edtxtPuntos = dialogView.findViewById<EditText>(R.id.edtxt_ptsCanjearUsuario)
            val txtPuntosUsuarioActuales = dialogView.findViewById<TextView>(R.id.txt_puntosUsuarioActuales)

            val mapaUsuarios = mutableMapOf<String, String>() // mapa nombre_usuario - uid
            val listaNombres = mutableListOf("Selecciona un alumno")

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

                // con todos estos datos, se crea el alertdialog
                AlertDialog.Builder(context)
                    .setTitle("Canjear puntos del usuario")
                    .setView(dialogView)
                    .setPositiveButton("Canjear") { _, _ ->
                        val nombreSeleccionado = spinner.selectedItem?.toString()
                        val puntosACanjear = edtxtPuntos.text.toString().toIntOrNull()

                        // validaciones
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

        // boton resetear huevos gallinas

        // -------------------------------- //
        // ----- BOTON RESETEAR HUEVOS ---- //
        // -------------------------------- //

        /*
        Este botón pone el atributo "huevos" de todas las gallinas del nodo a 0
         */
        btnResetearHuevos.setOnClickListener {

            AlertDialog.Builder(context)
                .setTitle("Resetear huevos")
                .setMessage("¿¿Estás segura de que quieres reiniciar a 0 los huevos de TODAS las gallinas??")
                .setPositiveButton("Sí, resetear!!") { _, _ ->
                    val gallinasRef = FirebaseDatabase.getInstance().reference.child("gallinas")

                    gallinasRef.get().addOnSuccessListener { snapshot ->
                        // recorre todas las gallinas cambiando el valor total_huevos a 0
                        for (gallinaSnap in snapshot.children) {
                            gallinaSnap.ref.child("total_huevos").setValue(0)
                        }
                        Utils.mostrarDialogoInformativo(
                            context,
                            "¡¡Huevos reseteados correctamente!! ¡¡EggStadisticas comenzando de cer0!!",
                            "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fparty.gif?alt=media&token=754a138f-51e5-4cdd-88fd-c8decf7fbfd7"
                        )

                    }.addOnFailureListener {
                        Utils.mostrarDialogoInformativo(
                            context,
                            "Oopsie woopsie... Ha habido un error al resetear los huevos: ${it.message} ",
                            "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                        )

                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

    }

}


