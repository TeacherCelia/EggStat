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

    private val _listaGallinas = MutableLiveData<List<Gallina>>()
    val listaGallinas: LiveData<List<Gallina>> = _listaGallinas

    private val firebaseDBref = FirebaseDatabase.getInstance().reference.child("gallinas")

    //para controlar errores de visualización
    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    init {
        cargarDatosGallinas()
    }

    private fun cargarDatosGallinas() {
        firebaseDBref.addValueEventListener(object : ValueEventListener {
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

    // otros metodos
}