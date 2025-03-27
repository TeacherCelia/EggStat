package theteachercelia.eggstatv1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
//import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    //private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        // Si el usuario ya está logueado → ir a MainActivity
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }


        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.edtxt_email)
        val passwordInput = findViewById<EditText>(R.id.edtxt_pass)
        val loginButton = findViewById<Button>(R.id.btn_iniciarSesion)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Autenticar con Firebase
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

         */
        setContentView(R.layout.activity_login)

        val botonLogin = findViewById<Button>(R.id.btn_iniciarSesion)
        val botonPruebas = findViewById<Button>(R.id.btn_Pruebas)

        botonLogin.setOnClickListener {
            Toast.makeText(this, "Aquí irá el login real", Toast.LENGTH_SHORT).show()
            // configurar aqui firebase tras pruebas
        }

        botonPruebas.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}