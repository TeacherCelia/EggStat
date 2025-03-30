package theteachercelia.eggstatv1.ui.control

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.databinding.FragmentControlBinding
import theteachercelia.eggstatv1.databinding.FragmentEstadisticasBinding
import theteachercelia.eggstatv1.ui.estadisticas.EstadisticasViewModel

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    // botones para agregar equipos y gallinas
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: meter un boton de agregar usuarios

        binding.btnAgregarGallina.setOnClickListener {
            Toast.makeText(requireContext(), "Aquí se añadirá una gallina", Toast.LENGTH_SHORT).show()
        }

        binding.btnAgregarEquipo.setOnClickListener {
            Toast.makeText(requireContext(), "Aquí se añadirá un equipo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}