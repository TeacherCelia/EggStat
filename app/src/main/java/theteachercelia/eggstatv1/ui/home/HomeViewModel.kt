package theteachercelia.eggstatv1.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import theteachercelia.eggstatv1.bd.Equipo
import theteachercelia.eggstatv1.bd.Usuario

class HomeViewModel : ViewModel() {
    /*
    Obtiene los datos del usuario autenticado y su equipo desde Firebase y los muestra
    con el LiveData.
     */

    // livedatas
    private val _nombreUsuario = MutableLiveData<String>()
    val nombreUsuario: LiveData<String> = _nombreUsuario

    private val _puntosUsuario = MutableLiveData<Int>()
    val puntosUsuario: LiveData<Int> = _puntosUsuario

    private val _puntosEquipo = MutableLiveData<Int>()
    val puntosEquipo: LiveData<Int> = _puntosEquipo

    private val _urlImagenEquipo = MutableLiveData<String>()
    val urlImagenEquipo: LiveData<String> = _urlImagenEquipo

    private val _nombreEquipo = MutableLiveData<String>()
    val nombreEquipo: LiveData<String> = _nombreEquipo

    // añadimos un livedata de error
    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    // instanciamos firebase (auth y database)
    private val firebaseDB = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()

    // iniciamos la carga de los datos llamando al metodo que creamos abajo
    init {
        cargarDatosUsuario()
    }
    // metodo para cargar los datos del usuario
    private fun cargarDatosUsuario() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val usuarioBD = firebaseDB.child("usuarios").child(uid)

        // usamos [addvalueeventlistener] para visualizar los datos en tiempo real
        usuarioBD.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuario = snapshot.getValue(Usuario::class.java)

                if (usuario != null) {
                    //actualizamos nombre y puntos de usuario
                    _nombreUsuario.value = usuario.nombre_usuario
                    _puntosUsuario.value = usuario.puntos_usuario

                    //consultamos de qué equipo es el usuario y actualizamos los puntos de ese equipo
                    val equipoID = usuario.equipo_id
                    val equipoRef = firebaseDB
                        .child("equipo")
                        .child(equipoID.lowercase().replace("\\s+".toRegex(), ""))

                    equipoRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val equipo = snapshot.getValue(Equipo::class.java)
                            _puntosEquipo.value = equipo?.puntos_equipo ?: 0
                            _urlImagenEquipo.value = equipo?.url_imagen_equipo ?: ""
                            _nombreEquipo.value = equipo?.nombre_equipo ?: ""
                        }

                        override fun onCancelled(error: DatabaseError) {
                            if (FirebaseAuth.getInstance().currentUser != null){
                                _mensajeError.value = "Oopsie woopsie... Error al obtener los datos del equipo: ${error.message}"
                            }

                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (FirebaseAuth.getInstance().currentUser != null){
                    _mensajeError.value = "Oopsie woopsie... Error al obtener los datos del usuario: ${error.message}"
                }

            }
        })


    }
}