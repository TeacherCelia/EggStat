package theteachercelia.eggstatv1.ui.estancias

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import theteachercelia.eggstatv1.bd.Estancia
import theteachercelia.eggstatv1.utils.Utils

class EstanciasViewModel : ViewModel() {

    // instanciamos Firebase
    private val firebaseBD = FirebaseDatabase.getInstance().reference

    // visualizamos los datos con livedata, guardando un mapa con cada estancia
    private val _mapaEstancias = MutableLiveData<Map<String, Estancia>>()
    val mapaEstancias: LiveData<Map<String, Estancia>> = _mapaEstancias //esta variable conecta con el fragment

    // para controlar errores de visualización
    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    init {
        cargarDatosEstancias()
    }

    //metodo para cargar los datos de las estancias
    private fun cargarDatosEstancias(){

        val estanciasBD = firebaseBD.child("estancia") // accedemos al nodo estancia

        // para que cada vez que cambien los datos en Firebase, "se avise"
        estanciasBD.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { // entregamos una "foto" del nodo
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

            override fun onCancelled(error: DatabaseError) {
                _mensajeError.value = "Error en la base de datos: ${error.message}"
            }
        })

    }


}
