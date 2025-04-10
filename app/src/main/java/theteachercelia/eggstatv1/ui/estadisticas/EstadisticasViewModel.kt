package theteachercelia.eggstatv1.ui.estadisticas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import theteachercelia.eggstatv1.bd.Equipo
import theteachercelia.eggstatv1.bd.Gallina

class EstadisticasViewModel : ViewModel() {

    // instanciamos firebase (auth y database)
    private val firebaseDB = FirebaseDatabase.getInstance().reference

    // livedata de los datos a visualizar
    private val _mapaGallinas = MutableLiveData<Map<String,Int>>()
    val mapaGallinas : LiveData<Map<String,Int>> = _mapaGallinas

    private val _mapaEquipos = MutableLiveData<Map<String,Int>>()
    val mapaEquipos : LiveData<Map<String,Int>> = _mapaEquipos

    init {

        cargarDatosEstadisticas()// o lo que necesites
    }

    //--- carga de datos
    private fun cargarDatosEstadisticas() { //cargamos los datos del livedata
        val gallinasBD = firebaseDB.child("gallinas")
        val equiposBD = firebaseDB.child("equipo")

        // cargar datos de gallinas
        gallinasBD.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //creamos el mapa de datos para el livedata
                val mapGallina = mutableMapOf<String,Int>()
                for (gallinaSnap in snapshot.children) {
                    val nombre = gallinaSnap.child("nombre_gallina").getValue(String::class.java)
                    val huevos = gallinaSnap.child("total_huevos").getValue(Int::class.java) ?: 0
                    if (!nombre.isNullOrEmpty()) {
                        mapGallina[nombre] = huevos
                    }
                    _mapaGallinas.value = mapGallina
                }

            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // cargar datos de equipos
        equiposBD.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mapa = mutableMapOf<String, Int>()
                for (equipoSnap in snapshot.children) {
                    val nombre = equipoSnap.child("nombre_equipo").getValue(String::class.java)
                    val puntos = equipoSnap.child("puntos_equipo").getValue(Int::class.java) ?: 0
                    if (!nombre.isNullOrEmpty()) {
                        mapa[nombre] = puntos
                    }
                }
                _mapaEquipos.value = mapa
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    //--- metodos extra si se necesitan


}