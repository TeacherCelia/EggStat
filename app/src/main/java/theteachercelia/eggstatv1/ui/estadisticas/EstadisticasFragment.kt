package theteachercelia.eggstatv1.ui.estadisticas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import theteachercelia.eggstatv1.R
import theteachercelia.eggstatv1.utils.Utils

class EstadisticasFragment : Fragment() {


    private lateinit var viewModel: EstadisticasViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_estadisticas,container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //instancia del viewmodel
        viewModel = ViewModelProvider(this)[EstadisticasViewModel::class.java]

        //referencias a views
        val graficoCircular = view.findViewById<PieChart>(R.id.pieChart_Gallinas)
        val graficoBarras = view.findViewById<BarChart>(R.id.barChart_Equipos)
        val coloresPersonalizados = Utils.obtenerColoresPersonalizados(requireContext()) //para poner los colores personalizados a los gráficos

        //observadores
        viewModel.mapaGallinas.observe(viewLifecycleOwner) { mapa ->
            actualizarPieChart(mapa, graficoCircular, coloresPersonalizados)
        }

        viewModel.mapaEquipos.observe(viewLifecycleOwner) { mapa ->
            actualizarBarChart(mapa, graficoBarras, coloresPersonalizados)
        }

        // Lógica UI (listeners, etc)


    }


    //otros metodos
    private fun actualizarPieChart(mapa: Map<String, Int>, pieChart: PieChart, colores: List<Int>) {
        //1-- convertir en datos de PieEntry el mapa de gallinas
        val datosGallinas = mapa.map { (nombreGallina, totalHuevos) ->
            PieEntry(totalHuevos.toFloat(), nombreGallina)
        }

        //2-- crear dataset que queremos representar
        val dataSetGallinas = PieDataSet(datosGallinas, "Gallinas")

        //3-- aplicar colores
        dataSetGallinas.colors = colores

        //4-- asociar datos al gráfico
        //tamaño numero huevos
        val pieData = PieData(dataSetGallinas)
        pieData.setValueTextSize(16f) // tamaño del número

        pieChart.data = pieData

        //5-- modificaciones visuales

        // quitar descripcion por defecto
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false

        pieChart.invalidate() //refrescar grafico
        pieChart.requestLayout()// refrescar gráfico
    }

    private fun actualizarBarChart(mapa: Map<String, Int>, barChart: BarChart, colores: List<Int>) {
        //1-- convertir en datos de BarEntry el mapa de equipos
        val datosEquipos = mapa.entries.mapIndexed { indice, (nombreEquipo, puntos) ->
            BarEntry(indice.toFloat(), puntos.toFloat())
        }

        //2-- crear dataset que queremos representar
        val dataSetEquipos = BarDataSet(datosEquipos, "Equipos")

        //3-- aplicar colores
        //dataSetEquipos.setColors(*ColorTemplate.MATERIAL_COLORS)
        dataSetEquipos.colors = colores

        //4-- le ponemos los nombres de equipo abajo
        val labels = mapa.keys.toList()

        //5-- asociar datos al grafico
        val barData = BarData(dataSetEquipos)

        // tamaño datos
        barData.setValueTextSize(14f)

        // eje X
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.labelRotationAngle = -45f // inclina un poco

        // quitar descripcion por defecto
        barChart.description.isEnabled = false

        // config de leyenda
        barChart.legend.isEnabled = false

        barChart.data = barData

        // refrescar grafico
        barChart.invalidate()
        // Espacio extra abajo para que los nombres no se corten
        barChart.setExtraBottomOffset(40f)
        barChart.requestLayout()
    }
}