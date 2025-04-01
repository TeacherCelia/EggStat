package theteachercelia.eggstatv1.ui.gallinas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import theteachercelia.eggstatv1.R

class GallinasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val gallinasViewModel =
            ViewModelProvider(this).get(GallinasViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_gallinas, container, false)
        return view


    }

}