package theteachercelia.eggstatv1.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeViewModel : ViewModel() {

    // instanciamos firebase (auth y database)
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    // visualizamos los datos del nombre de usuario, de sus puntos y los de su equipo
    private val _nombreUsuario = MutableLiveData<String>()
    val nombreUsuario: LiveData<String> = _nombreUsuario

    private val _puntosUsuario = MutableLiveData<Int>()
    val puntosUsuario: LiveData<Int> = _puntosUsuario

    private val _puntosEquipo = MutableLiveData<Int>()
    val puntosEquipo: LiveData<Int> = _puntosEquipo

    // iniciamos la carga de los datos llamando al metodo que creamos abajo
    init {
        cargarDatosUsuario()
    }
    // metodo para cargar los datos del usuario
    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return

        val usuarioRef = database.child("usuarios").child(uid)

        usuarioRef.get().addOnSuccessListener { snapshot ->
            val nombre = snapshot.child("nombre_usuario").getValue(String::class.java) ?: ""
            val puntos = snapshot.child("puntos_usuario").getValue(Int::class.java) ?: 0
            val equipoId = snapshot.child("equipo_id").getValue(String::class.java) ?: ""

            // cargamos los datos de nombre y puntos
            _nombreUsuario.value = nombre
            _puntosUsuario.value = puntos

            // **REL** para el equipo tenems que buscar en la base de datos la relación entre las entidades, y así sacamos los puntos de equipo
            if (equipoId.isNotEmpty()) {
                val equipoRef = database.child("equipo").child(equipoId.lowercase().replace("\\s+".toRegex(), ""))
                equipoRef.child("puntos_equipo").get().addOnSuccessListener { equipoSnapshot ->
                    val puntosEquipo = equipoSnapshot.getValue(Int::class.java) ?: 0
                    _puntosEquipo.value = puntosEquipo
                }
            }
        }
    }
}