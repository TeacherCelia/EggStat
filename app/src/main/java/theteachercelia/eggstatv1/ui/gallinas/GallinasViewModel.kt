package theteachercelia.eggstatv1.ui.gallinas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GallinasViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Aquí se mostrarán las gallinas"
    }
    val text: LiveData<String> = _text
}