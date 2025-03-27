package theteachercelia.eggstatv1.ui.estancias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import theteachercelia.eggstatv1.databinding.FragmentEstanciasBinding

class EstanciasFragment : Fragment() {

    private var _binding: FragmentEstanciasBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val estanciasViewModel =
            ViewModelProvider(this).get(EstanciasViewModel::class.java)

        _binding = FragmentEstanciasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        estanciasViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}