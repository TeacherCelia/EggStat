package theteachercelia.eggstatv1.ui.gallinas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import theteachercelia.eggstatv1.bd.Gallina

class GallinasViewModel : ViewModel() {

    /*
    Viewmodel que carga los datos de las gallinas obtenidos de Firebase.
    Escucha a tiempo real el nodo "gallinas", y muestra a través de LiveData la lista de gallinas,
    además de informar al fragmentgallinas de si hay un error al obtener los datos.
    */

    // livedata
    private val _listaGallinas = MutableLiveData<List<Gallina>>()
    val listaGallinas: LiveData<List<Gallina>> = _listaGallinas

    // para controlar errores de visualización
    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    // firebase
    private val firebaseDBgallinas = FirebaseDatabase.getInstance().reference.child("gallinas")


    init {
        cargarDatosGallinas()
    }

    private fun cargarDatosGallinas() {
        firebaseDBgallinas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gallinas = mutableListOf<Gallina>()
                for (gallinaSnap in snapshot.children) {
                    val gallina = gallinaSnap.getValue(Gallina::class.java)
                    gallina?.let { gallinas.add(it) }
                }
                _listaGallinas.value = gallinas
            }

            override fun onCancelled(error: DatabaseError) {
                _mensajeError.value = "Error al cargar gallinas: ${error.message}"

            }

        })
    }
}