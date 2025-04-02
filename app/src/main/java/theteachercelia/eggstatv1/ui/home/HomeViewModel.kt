package theteachercelia.eggstatv1.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import theteachercelia.eggstatv1.bd.Equipo
import theteachercelia.eggstatv1.bd.Usuario

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
            val usuario = snapshot.getValue(Usuario::class.java)

            if (usuario != null) {
                _nombreUsuario.value = usuario.nombre_usuario
                _puntosUsuario.value = usuario.puntos_usuario

                // obtenemos el id del equipo del usuario
                val equipoID = usuario.equipo_id

                // **REL** para el equipo tenems que buscar en la base de datos la relación entre las entidades, y así sacamos los puntos de equipo
                if (!equipoID.isNullOrEmpty()) {
                    //normalizador
                    val equipoRef = database.child("equipo").child(equipoID.lowercase().replace("\\s+".toRegex(), ""))

                    // leemos datos del equipo
                    equipoRef.get().addOnSuccessListener { equipoSnapshot ->
                        //convertimos snapshot en objeto equipo
                        val puntosEquipo = equipoSnapshot.getValue(Equipo::class.java)

                        // se actualizan los puntos
                        _puntosEquipo.value = puntosEquipo?.puntos_equipo ?: 0
                    }
                }
            }

        }
    }
}