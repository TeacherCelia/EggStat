package theteachercelia.eggstatv1.ui.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ControlViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Aquí apareceran los botones de control que solo los profesores podrán usar"
    }
    val text: LiveData<String> = _text
}