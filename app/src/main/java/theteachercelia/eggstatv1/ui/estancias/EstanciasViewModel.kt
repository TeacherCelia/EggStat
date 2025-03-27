package theteachercelia.eggstatv1.ui.estancias

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EstanciasViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _text = MutableLiveData<String>().apply {
        value = "Esta será la pantalla las estancias, donde se podrán registrar cambios"
    }
    val text: LiveData<String> = _text
}
