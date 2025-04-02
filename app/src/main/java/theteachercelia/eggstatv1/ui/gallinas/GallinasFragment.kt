package theteachercelia.eggstatv1.ui.gallinas

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.bd.Gallina

class GallinasFragment : Fragment() {

    private lateinit var viewModel: GallinasViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_gallinas, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //instanciamos viewmodel y el grid
        viewModel = ViewModelProvider(this)[GallinasViewModel::class.java]

        //referencias a views
        val gridLayout = view.findViewById<GridLayout>(R.id.gridGallinas)

        //observadores del viewmodel
        viewModel.listaGallinas.observe(viewLifecycleOwner){ listagallinas ->
            gridLayout.removeAllViews()

            for (gallina in listagallinas){
                val avatarview = layoutInflater.inflate(R.layout.avatar_gallina,gridLayout,false)
                val imgGallina = avatarview.findViewById<ImageView>(R.id.img_Gallina)
                val nombreGallina = avatarview.findViewById<TextView>(R.id.txt_NombreGallina)

                nombreGallina.text = gallina.nombre_gallina

                avatarview.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle(gallina.nombre_gallina)
                        .setMessage(
                            "Raza: ${gallina.raza}\n" +
                                    "Edad: ${gallina.edad} años\n" +
                                    "Huevos: ${gallina.total_huevos} 🥚"
                        )
                        .setPositiveButton("Cerrar", null)
                        .show()
                }

                Glide.with(this)
                    .load(gallina.foto_url)
                    .into(imgGallina)


                gridLayout.addView(avatarview)
            }
        }

        //lógica UI (listeners, etc)




    }





}