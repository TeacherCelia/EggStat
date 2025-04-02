package theteachercelia.eggstatv1.ui.gallinas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import theteachercelia.eggstatv1.bd.Gallina

class GallinasViewModel : ViewModel() {

    private val _listaGallinas = MutableLiveData<List<Gallina>>()
    val listaGallinas: LiveData<List<Gallina>> = _listaGallinas

    private val firebaseDBref = FirebaseDatabase.getInstance().reference.child("gallinas")

    init {
        cargarGallinas()
    }

    private fun cargarGallinas() {
        firebaseDBref.get().addOnSuccessListener { snapshot ->
            val gallinas = mutableListOf<Gallina>()
            for (gallinaSnap in snapshot.children) {
                val gallina = gallinaSnap.getValue(Gallina::class.java)
                gallina?.let { gallinas.add(it) }
            }
            _listaGallinas.value = gallinas
        }
    }

    // otros metodos
}