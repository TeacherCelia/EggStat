package theteachercelia.eggstatv1.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // ----------- creación del menú superior de configuracion ------------- //

        // TODO: modificar para que solo los profesores puedan abrirlo

        //creación del menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)
            }

            // al seleccionarlo, vamos al fragmentcontrol
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.accion_abrirControl -> {
                        findNavController().navigate(R.id.home_control) // fragment control desde el navigation controller
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // TODO: PRUEBA- cambiar al hacer la app funcional
        binding.txtSaludoUsuario.text = "¡Hola, Celia!"
        binding.txtSaludoUsuario.text = "Tienes 0 puntos"
        binding.txtPuntosEquipo.text = "Tu equipo tiene 0 puntos"

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}