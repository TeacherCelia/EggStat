package theteachercelia.eggstatv1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import theteachercelia.eggstatv1.bd.Usuario
import theteachercelia.eggstatv1.utils.Utils

class LoginActivity : AppCompatActivity() {
    /*
    Activity que aparecerá al abrir la app si no hay usuario logueado. Dispone de los siguientes
    componentes para interactuar:
    - EditText para introducir el email o usuario
    - EditText de Contraseña
    - Botón iniciar sesión
    - Texto clicable "olvidé contraseña"
    - Botón crear cuenta profesor (para crear una cuenta con una clave seccreta)
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // instancia a firebaseBD y auth
        val auth = FirebaseAuth.getInstance()
        val firebaseBD = FirebaseDatabase.getInstance().reference

        // referenciamos los componentes de la layout
        val emailEntradaTxt = findViewById<EditText>(R.id.edtxt_email)
        val passwordEntradaTxt = findViewById<EditText>(R.id.edtxt_pass)
        val botonLogin = findViewById<Button>(R.id.btn_iniciarSesion)
        val olvidePass = findViewById<TextView>(R.id.txt_olvidePass)
        val btnCrearProfesor = findViewById<Button>(R.id.btn_CrearProfesor)

        // si el usuario ya está logueado o no es anónimo, va a MainActivity
        val usuarioActual = auth.currentUser
        if (usuarioActual != null && !usuarioActual.isAnonymous) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // -------------------------- //
        // ----- BOTON DE LOGIN ----- //
        // -------------------------- //

        botonLogin.setOnClickListener {
            // variables para obtener los datos de los editText
            val input = emailEntradaTxt.text.toString().trim()
            val password = passwordEntradaTxt.text.toString()

            // si no se rellenan todos los campos, se muestra un Toast de advertencia y no se hace nada
            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "¡¡Rellena todos los campos!!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // lógica de validación Profesor - Alumno
            val email = if (esEmail(input)) {
                input // si se loguea con email, es profesor, no hace falta añadirle @eggstat.com al final
            } else {
                "$input@eggstat.com" // si no es email, es alumno, se le añade @eggstat.com al final
            }

            // lógica de login
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) { // si es correcto, el usuario se dirige a MainActivity logueado
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else { // si no es correcto, se muestra dialog informativo con el error
                        Utils.mostrarDialogoInformativo(
                            this,
                            "Oopsie woopsie... Error en login: ${task.exception?.message}",
                            "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                        )
                    }
                }
        }

        // ----------------------------- //
        // ----- OLVIDÉ CONTRASEÑA ----- //
        // ----------------------------- //

        olvidePass.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("¿Eres profe o alumno?")
            builder.setMessage("Selecciona tu rol para continuar")

            // si elige profe, enviamos un email al email que se indique en el edittext
            builder.setPositiveButton("Soy profe") { _, _ ->
                val input = EditText(this)
                input.hint = "Introduce el email con el que te registraste"

                AlertDialog.Builder(this)
                    .setTitle("Recuperar contraseña")
                    .setView(input)
                    .setPositiveButton("Enviar") { _, _ ->
                        val email = input.text.toString().trim()
                        if (email.isNotEmpty()) {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnSuccessListener {
                                    Utils.mostrarDialogoInformativo(
                                        this,
                                        "Si estabas registrado... ¡Un correo ha sido enviado a $email! ¡Revisa tu bandeja de entrada! Si no tienes nada, ¡es porque no estabas registrado! ¡Regístrate!",
                                        "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcohete.gif?alt=media&token=fc33791f-d852-4eb6-8de1-905d9633bae6"
                                    )

                                }
                                .addOnFailureListener {
                                    Utils.mostrarDialogoInformativo(
                                        this,
                                        "Oopsie woopsie... Ha habido un error: ${it.message} ",
                                        "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                                    )

                                }
                        } else {
                            Toast.makeText(this, "¡¡Escribe un email!!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            // si elige alumno, mostramos un mensaje sencillo
            builder.setNegativeButton("Soy alumno") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Oh vaya...")
                    .setMessage("¡Habla con tu profe para que te recuerde tu contraseña!")
                    .setPositiveButton("Entendido") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            builder.show()

        }

        // -------------------------------- //
        // ----- BOTÓN CREAR PROFESOR ----- //
        // -------------------------------- //

        btnCrearProfesor.setOnClickListener {
            // referenciamos el dialog que se abrirá al pulsar sobre el botón
            val dialogView = layoutInflater.inflate(R.layout.dialog_crear_profesor, null) // se abre el dialog

            // instanciamos todos los componentes del dialog con los id de dialog_crear_profesor.xml
            val inputEmail = dialogView.findViewById<EditText>(R.id.edtxt_inputEmail)
            val inputUsuario = dialogView.findViewById<EditText>(R.id.edtxt_inputUsuario)
            val inputPassword = dialogView.findViewById<EditText>(R.id.edtxt_inputPassword)
            val inputRepetirPass = dialogView.findViewById<EditText>(R.id.edtxt_inputRepetirPassword)
            val spinnerEquipos = dialogView.findViewById<Spinner>(R.id.spinner_Equipo)
            val inputClaveSecreta = dialogView.findViewById<EditText>(R.id.edtxt_inputClaveSecreta)

            // para cargar los equipos en el spinner
            val equiposRef = firebaseBD.child("equipo")
            val listaEquipos = mutableListOf("Selecciona un equipo")

            // como las reglas de Firebase están cerradas a usuarios registrados, se realiza
            // un login anónimo para poder acceder a la base de datos
            if (auth.currentUser == null) {
                auth.signInAnonymously()
            }

            // cargar los equipos en el spinner
            equiposRef.get()
                .addOnSuccessListener { snapshot ->

                if (snapshot.exists()){ //para añadir a "selecciona un equipo" el resto de equipos que hay en el nodo "equipo" de Firebase
                    for (equipoSnap in snapshot.children) {
                        val nombreEquipo = equipoSnap.child("nombre_equipo").getValue(String::class.java)
                        if (!nombreEquipo.isNullOrEmpty()) {
                            listaEquipos.add(nombreEquipo)
                        }
                    }
                    // adapter del spinner (para "traducir" los datos de la lista de equipos a datos legibles por el spinner)
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaEquipos)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerEquipos.adapter = adapter
                }

                // se crea el dialog
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Registro de profesor")
                    .setView(dialogView)
                    .setPositiveButton("Crear", null) // utilizamos "null" para poder controlar el botón manualmente más adelante
                    .setNegativeButton("Cancelar", null)
                    .create()

                // cuando el dialogo se muestre... lógica del botón "crear"
                dialog.setOnShowListener {
                    val btnCrear = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    btnCrear.setOnClickListener {
                        // variables de todos los inputs del dialog
                        val email = inputEmail.text.toString().trim()
                        val usuario = inputUsuario.text.toString().trim()
                        val password = inputPassword.text.toString().trim()
                        val repetirPass = inputRepetirPass.text.toString().trim()
                        val equipoSeleccionado = spinnerEquipos.selectedItem?.toString() ?: ""
                        val clave = inputClaveSecreta.text.toString().trim()

                        // para asegurarnos de que se rellenan todos los campos
                        if (email.isEmpty() || usuario.isEmpty() || password.isEmpty() || repetirPass.isEmpty() || clave.isEmpty() || equipoSeleccionado == "Selecciona un equipo") {
                            Toast.makeText(this, "¡¡Rellena todos los campos!!", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // para asegurarnos de que las contraseñas coiniden
                        if (password != repetirPass) {
                            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // clave especial y única (que vendrá en el manual de usuario) para crear un profesor. Si no, se muestra un dialogo malvado
                        if (clave != "Pr0f3k3y") {
                            Utils.mostrarDialogoInformativo(
                                this,
                                "¡¡Clave incorrecta!! Jamás podrás adivinarla si no eres profe... ¡¡muajaja!!",
                                "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fdemonio.gif?alt=media&token=f884af32-e754-4dc4-9b39-257a04476a89"
                            )
                            return@setOnClickListener
                        }

                        // si pasa todas las verificaciones, se crea un usuario en FirebaseAuth
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { result ->
                                val uid = result.user?.uid ?: return@addOnSuccessListener
                                val nuevoProfesor = Usuario(
                                    nombre_usuario = usuario,
                                    email = email,
                                    equipo_id = equipoSeleccionado,
                                    rol = "profesor",
                                    puntos_usuario = 0
                                )

                                // se añade al nodo "usuarios" de FirebaseDatabase
                                firebaseBD.child("usuarios").child(uid).setValue(nuevoProfesor)
                                    .addOnSuccessListener {
                                        // se muestra dialog informativo si tdo va bien
                                        Utils.mostrarDialogoInformativo(
                                            this,
                                            "¡Nuevo profe añadido!",
                                            "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Femoji_gafas.gif?alt=media&token=4598f3c1-c824-464c-a42f-dfad066703a0"
                                        )
                                        dialog.dismiss()
                                    }
                                    .addOnFailureListener {
                                        //se muestra dialogo informativo mostrando un error si no se puede añadir a la BD
                                        Utils.mostrarDialogoInformativo(
                                            this,
                                            "Oopsie woopsie... Error al guardar en la base de datos :(",
                                            "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                                        )

                                    }
                            }
                            .addOnFailureListener {
                                // se muestra un dialogo informativo con un error si algo ha salido mal
                                Utils.mostrarDialogoInformativo(
                                    this,
                                    "Oopsie woopsie... Error creando el usuario: ${it.message} ",
                                    "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                                )

                            }
                    }
                }
                // se muestra el dialog
                dialog.show()
            }
                .addOnFailureListener { error ->
                    Utils.mostrarDialogoInformativo(
                        this,
                        "Oopsie woopsie... Error al acceder a la base de datos: ${error.message} ",
                        "https://firebasestorage.googleapis.com/v0/b/eggstatdb.firebasestorage.app/o/img_recurso%2Fcalavera.gif?alt=media&token=7cc9cba8-b10d-48c4-9ad4-da0210f713cf"
                    )
                }


        }

    }

    // función para diferenciar entre profesor (con email) y alumno
    fun esEmail(input: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }
}