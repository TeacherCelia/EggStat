package theteachercelia.eggstatv1.ui.estancias

import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.bd.Estancia

class EstanciasViewModel : ViewModel() {

    // instanciamos Firebase y FirebaseAuth
    private val firebaseBD = FirebaseDatabase.getInstance().reference

    // visualizamos los datos con livedata, guardando un mapa con cada estancia
    private val _mapaEstancias = MutableLiveData<Map<String, Estancia>>()
    val mapaEstancias: LiveData<Map<String, Estancia>> = _mapaEstancias //esta variable conecta con el fragment

    init {
        cargarDatosEstancias()
    }

    //metodo para cargar los datos de las estancias
    private fun cargarDatosEstancias(){

        val estanciasBD = firebaseBD.child("estancia") // accedemos al nodo estancia

        // para que cada vez que cambien los datos en Firebase, "se avise"
        estanciasBD.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) { // entregamos una "foto" del nodo
                // creamos un mapa del nodo estancia para devolver la clave (nombre) y el valor (objeto estancia)
                val mapa = mutableMapOf<String, Estancia>()

                for (hijo in snapshot.children) {
                    val estancia = hijo.getValue(Estancia::class.java)
                    val clave = hijo.key

                    if (estancia != null && clave != null) {
                        mapa[clave] = estancia
                    }
                }

                _mapaEstancias.value = mapa // actualizamos el livedata con el mapa completo
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                //TODO: hacer logs o mostrar un toast desde el fragment
            }
        })

    }


}
