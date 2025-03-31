package theteachercelia.eggstatv1.ui.control

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.databinding.FragmentControlBinding
//nuevos imports para auth
import android.app.AlertDialog
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    // botones para agregar equipos y gallinas
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference

        // boton agregar usuario
        binding.btnAgregarUsuario.setOnClickListener {
            // usamos un dialogview para agregar usuarios
            val context = requireContext()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_crear_usuario, null)

            // identificar las partes del dialog con los ids del dialog_crear_usuario.xml
            val inputUsuario = dialogView.findViewById<EditText>(R.id.edtxt_CrearUsuario)
            val inputPass = dialogView.findViewById<EditText>(R.id.edtxt_CrearContrasena)
            val inputEquipo = dialogView.findViewById<EditText>(R.id.edtxt_CrearEquipo)

            AlertDialog.Builder(context)
                .setTitle("Crear nuevo usuario")
                .setView(dialogView)
                .setPositiveButton("Crear") { _, _ ->
                    val usuario = inputUsuario.text.toString().trim()
                    val password = inputPass.text.toString()
                    val equipo = inputEquipo.text.toString().trim()

                    // si algún campo está vacío
                    if (usuario.isEmpty() || password.isEmpty() || equipo.isEmpty()) {
                        Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // como FirebaseAuth requiere un email, y los alumnos no pueden tener, se le pone este campo de forma automatica
                    val emailFake = "$usuario@eggstat.com"

                    auth.createUserWithEmailAndPassword(emailFake, password)
                        .addOnSuccessListener { result ->
                            val uid = result.user?.uid ?: return@addOnSuccessListener
                            val nuevoUsuario = mapOf(
                                "nombre_usuario" to usuario,
                                "rol" to "alumno",
                                "equipo_id" to equipo,
                                "puntos_usuario" to 0
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

        // TODO: implementar funcionalidad boton agregar gallina
        binding.btnAgregarGallina.setOnClickListener {
            Toast.makeText(requireContext(), "Aquí se añadirá una gallina", Toast.LENGTH_SHORT).show()
        }

        // TODO: implementar funcionalidad boton agregar equipo
        binding.btnAgregarEquipo.setOnClickListener {
            Toast.makeText(requireContext(), "Aquí se añadirá un equipo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}