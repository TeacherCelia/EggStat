package theteachercelia.eggstatv1.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Esta es la pantalla de inicio, donde se muestre tu nombre y los puntos que tienes."
    }
    val text: LiveData<String> = _text
}