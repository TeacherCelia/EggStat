package theteachercelia.eggstatv1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    // para diferenciar entre profesor (email) y alumno, hacemos esta funcion que identifica si es email
    fun esEmail(input: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // instanciar a firebase y auth
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference

        // referenciamos los componentes
        val emailEntradaTxt = findViewById<EditText>(R.id.edtxt_email)
        val passwordEntradaTxt = findViewById<EditText>(R.id.edtxt_pass)

        // Si el usuario ya está logueado va a MainActivity
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val botonPruebas = findViewById<Button>(R.id.btn_Pruebas)
        botonPruebas.visibility = View.GONE
        botonPruebas.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // ----- boton de login ----- //
        val botonLogin = findViewById<Button>(R.id.btn_iniciarSesion)
        botonLogin.setOnClickListener {
            val input = emailEntradaTxt.text.toString().trim()
            val password = passwordEntradaTxt.text.toString()

            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "¡¡Rellena todos los campos!!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = if (esEmail(input)) {
                input // si se loguea con email, es profesor, no hace falta añadirle @eggstat.com al final
            } else {
                "$input@eggstat.com" // si no es email, es alumno, se le añade @eggstat.com al final
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error en login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // ----- olvidé contraseña

        val olvidePass = findViewById<TextView>(R.id.txtOlvidasteContrasena)
        olvidePass.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("¿Eres profe o alumno?")
            builder.setMessage("Selecciona tu rol para continuar")

            // si elige profe
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
                                    Toast.makeText(this, "Correo enviado a $email", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(this, "¡¡Escribe un email!!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            // si elige alumno
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

        // ----- boton crear profesor, que abre un dialog para introducir los datos que crearán un usuario de rol profesor
        val btnCrearProfesor = findViewById<Button>(R.id.btn_CrearProfesor)
        btnCrearProfesor.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_crear_profesor, null) // se abre el dialog

            // instanciamos todos los componentes del dialog con los id de dialog_crear_profesor.xml
            val inputEmail = dialogView.findViewById<EditText>(R.id.edtxt_inputEmail)
            val inputUsuario = dialogView.findViewById<EditText>(R.id.edtxt_inputUsuario)
            val inputPassword = dialogView.findViewById<EditText>(R.id.edtxt_inputPassword)
            val inputRepetirPass = dialogView.findViewById<EditText>(R.id.edtxt_inputRepetirPassword)
            val spinnerEquipos = dialogView.findViewById<Spinner>(R.id.spinner_Equipo)
            val inputClaveSecreta = dialogView.findViewById<EditText>(R.id.edtxt_inputClaveSecreta)

            // para cargar los equipos en el spinner
            val equiposRef = database.child("equipo")
            val listaEquipos = mutableListOf("Selecciona un equipo")

            // cargar los equipos MENOS profesores
            equiposRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()){ //para añadir a "selecciona un equipo" el resto de equipos que hay en firebase "equipo"
                    for (equipoSnap in snapshot.children) {
                        val nombreEquipo = equipoSnap.child("nombre_equipo").getValue(String::class.java)
                        if (!nombreEquipo.isNullOrEmpty()) {
                            listaEquipos.add(nombreEquipo)
                        }
                    }
                    // Adaptador y asignar al Spinner
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaEquipos)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerEquipos.adapter = adapter
                }

                AlertDialog.Builder(this) //crear dialog con los siguientes atributos
                    .setTitle("Registro de profesor")
                    .setView(dialogView)
                    .setPositiveButton("Crear") { _, _ ->
                        val email = inputEmail.text.toString().trim() //añadimos trims para quitar espacios
                        val usuario = inputUsuario.text.toString().trim()
                        val password = inputPassword.text.toString()
                        val repetirPass = inputRepetirPass.text.toString().trim()
                        val equipoSeleccionado = spinnerEquipos.selectedItem?.toString() ?: ""
                        val clave = inputClaveSecreta.text.toString()

                        // nos aseguramos de que todos los campos estén rellenos
                        if (email.isEmpty() || usuario.isEmpty() || password.isEmpty() || repetirPass.isEmpty() || clave.isEmpty() || equipoSeleccionado == "Selecciona un equipo") {
                            Toast.makeText(this, "¡¡Rellena todos los campos!!", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        if (password != repetirPass) {
                            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        //si esta no es la clave, no se crea profesor
                        if (clave != "Pr0f3k3y") {
                            Toast.makeText(this, "¡¡Clave incorrecta!! Jamás podrás adivinarla si no eres profe... ¡¡muajaja!!", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        // crear cuenta en firebase con esos datos
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { result ->
                                val uid = result.user?.uid ?: return@addOnSuccessListener
                                val nuevoProfesor = mapOf(
                                    "nombre_usuario" to usuario,
                                    "email" to email,
                                    "equipo_id" to equipoSeleccionado,
                                    "rol" to "profesor",
                                    "puntos_usuario" to 0
                                )
                                // mensajes al añadir un usuario a la BD (success y failure)
                                database.child("usuarios").child(uid).setValue(nuevoProfesor)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "¡¡Nuevo profe añadido!!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Ha habido un error al añadir el profe a la BD... :(", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            // si firebase no acepta el registro
                            .addOnFailureListener {
                                it.printStackTrace() // pruebas para ver en el LOG el error concreto
                                Toast.makeText(this, "Error creando usuario: ${it.message}", Toast.LENGTH_LONG).show()
                            }

                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

        }

    }
}