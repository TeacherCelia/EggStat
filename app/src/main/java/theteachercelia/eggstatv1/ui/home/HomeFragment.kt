package theteachercelia.eggstatv1.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import theteachercelia.eggstatv1.LoginActivity
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.bd.Usuario

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_home,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        //referencias a views
        val txtSaludo = view.findViewById<TextView>(R.id.txt_saludoUsuario)
        val txtPuntosUsuario = view.findViewById<TextView>(R.id.txt_puntosUsuario)
        val txtPuntosEquipo = view.findViewById<TextView>(R.id.txt_puntosEquipo)
        val btnLogout = view.findViewById<Button>(R.id.btnLogOut)
        val imgEquipo = view.findViewById<ImageView>(R.id.img_equipoHome)
        val txtNombreEquipo = view.findViewById<TextView>(R.id.txt_nombreEquipo)


        //observadores de la view
        viewModel.nombreUsuario.observe(viewLifecycleOwner) { nombre ->
            txtSaludo.text = "Hola, $nombre!"
        }

        viewModel.puntosUsuario.observe(viewLifecycleOwner) { puntos ->
            txtPuntosUsuario.text = "Tienes $puntos puntos"
        }

        viewModel.puntosEquipo.observe(viewLifecycleOwner) { puntosEq ->
            txtPuntosEquipo.text = "Tu equipo tiene $puntosEq puntos"
        }

        viewModel.nombreEquipo.observe(viewLifecycleOwner) { nombre ->
            txtNombreEquipo.text = "Equipo $nombre"
        }

        //observador imagen de equipo de usuario con GLIDE
        viewModel.urlImagenEquipo.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .asGif()
                    .load(url)
                    .into(imgEquipo)
            }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

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
    }
}