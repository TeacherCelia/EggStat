package theteachercelia.eggstatv1.ui.huevos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HuevosViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Esta será la pantalla de los huevos, donde haya un buttonImage pulsable"
    }
    val text: LiveData<String> = _text
}