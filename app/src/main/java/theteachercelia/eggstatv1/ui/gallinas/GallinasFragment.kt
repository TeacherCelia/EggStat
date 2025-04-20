package theteachercelia.eggstatv1.ui.gallinas

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import theteachercelia.eggstatv1.R

class GallinasFragment : Fragment() {

    /*
    Fragment encargado de mostrar un Grid con todas las gallinas registradas en Firebase
    - Carga las gallinas desde GallinasViewModel y las muestra con imagen y nombre
    - Al pulsar sobre una gallina, se abre un dialog con su información detallada

    Además, se implementa el metodo "calcularEdad" que calcula la edad de la gallina con el dato
    de la fecha de nacimiento proporcionado en Firebase
    */

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


        //---- instancia del viewmodel
        viewModel = ViewModelProvider(this)[GallinasViewModel::class.java]

        //---- referencias a la view
        val gridLayout = view.findViewById<GridLayout>(R.id.gridGallinas)

        //---- observadores del viewmodel

        // observador del grid
        viewModel.listaGallinas.observe(viewLifecycleOwner){ listagallinas ->
            gridLayout.removeAllViews()

            for (gallina in listagallinas){
                val avatarview = layoutInflater.inflate(R.layout.dialog_avatar_gallina, gridLayout, false)
                val imgGallina = avatarview.findViewById<ImageView>(R.id.img_Gallina)
                val nombreGallina = avatarview.findViewById<TextView>(R.id.txt_NombreGallina)
                val edadTexto = calcularEdad(gallina.fecha_nacimiento) //llamada a la funcion de calcular


                // ponemos el nombre de gallina debajo
                nombreGallina.text = gallina.nombre_gallina

                avatarview.setOnClickListener {
                    val dialogView = layoutInflater.inflate(R.layout.dialog_datos_gallina, null)
                    val imgDialogGallina = dialogView.findViewById<ImageView>(R.id.img_avatarGallina)
                    val txtDatosGallina = dialogView.findViewById<TextView>(R.id.txt_datosGallina)

                    // Texto de los datos
                    val textoInfo = "Raza: ${gallina.raza}\n" + "Edad: $edadTexto\n" +
                            "Huevos: ${gallina.total_huevos}"

                    txtDatosGallina.text = textoInfo

                    //para mostrar la imagen en el Fragment
                    Glide.with(this)
                        .load(gallina.foto_url)
                        .into(imgDialogGallina)

                    AlertDialog.Builder(requireContext())
                        .setTitle(gallina.nombre_gallina)
                        .setView(dialogView)
                        .setPositiveButton("Cerrar", null)
                        .show()
                }

                //para mostrar la imagen en el Dialog
                Glide.with(this)
                    .load(gallina.foto_url)
                    .into(imgGallina)

                gridLayout.addView(avatarview)

            }
        }

        // observador del toast
        viewModel.mensajeError.observe(viewLifecycleOwner) { mensaje ->
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
        }


    }

    /*
    Calcula la edad de la gallina a partir de su fecha de nacimiento y devuelve la edad como texto
    ("X años") o un mensaje de error si el formato no es válido
     */
    private fun calcularEdad(fechaNacimiento: String): String {
        return try {
            val formatoFecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val fechaNac = formatoFecha.parse(fechaNacimiento)
            val hoy = java.util.Calendar.getInstance()
            val cumple = java.util.Calendar.getInstance()
            cumple.time = fechaNac

            var edad = hoy.get(java.util.Calendar.YEAR) - cumple.get(java.util.Calendar.YEAR)

            if (hoy.get(java.util.Calendar.DAY_OF_YEAR) < cumple.get(java.util.Calendar.DAY_OF_YEAR)) {
                edad-- // si aún no ha cumplido años este año
            }

            "$edad años"
        } catch (e: Exception) {
            "Edad desconocida"
        }
    }

}