package theteachercelia.eggstatv1.ui.estadisticas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EstadisticasViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _text = MutableLiveData<String>().apply {
        value = "Aqui aparecerán estadísticas en tiempo real de puntos, huevos, etc..."
    }
    val text: LiveData<String> = _text

}