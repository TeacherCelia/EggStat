package theteachercelia.eggstatv1.ui.home

import android.content.Intent
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import theteachercelia.eggstatv1.LoginActivity
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.bd.Usuario
import theteachercelia.eggstatv1.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    //usamos binding para no tener que utilizar el findviewbyid
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // visualizamos el ViewModel
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // ----------- creación del menú superior de configuracion ------------- //

        // instanciamos firebase auth y database
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference

        // solo si es profesor, se visualiza el menu de control de arriba
        val uid = auth.currentUser?.uid // para ello verificamos quien se logueó con el auth

        if (uid != null) {
            database.child("usuarios").child(uid).get().addOnSuccessListener { snapshot ->
                val usuario = snapshot.getValue(Usuario::class.java)
                if (usuario?.rol == "profesor") { // solo si es profe, creamos el menu
                    requireActivity().addMenuProvider(object : MenuProvider {
                        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                            menuInflater.inflate(R.menu.menu_home, menu)
                        }

                        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                            return when (menuItem.itemId) {
                                R.id.accion_abrirControl -> {
                                    findNavController().navigate(R.id.home_control)
                                    true
                                }
                                else -> false
                            }
                        }
                    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
                }
            }
        }

        // ***** OBSERVAMOS LOS LIVEDATA DEL HOMEVIEWMODEL ***** //

        // indicamos la raíz de los datos que visualizaremos
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // observadores
        viewModel.nombreUsuario.observe(viewLifecycleOwner) { nombre ->
            binding.txtSaludoUsuario.text = "¡Hola, $nombre!"
        }

        viewModel.puntosUsuario.observe(viewLifecycleOwner) { puntos ->
            binding.txtPuntosUsuario.text = "Tienes $puntos puntos"
        }

        viewModel.puntosEquipo.observe(viewLifecycleOwner) { puntosEquipo ->
            binding.txtPuntosEquipo.text = "Tu equipo tiene $puntosEquipo puntos"
        }

        binding.btnLogOut.setOnClickListener {
            // cerramos sesión en FirebaseAuth
            FirebaseAuth.getInstance().signOut()

            // volvemos a loginactivity
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // evitamos que el usuaro pueda volver con el boton atras
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}