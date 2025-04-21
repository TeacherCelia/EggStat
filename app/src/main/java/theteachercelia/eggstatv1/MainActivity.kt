package theteachercelia.eggstatv1

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        La MainActivity tiene dos partes visualmente diferenciadas:
        - el BottomNavigationview (nav_view), siempre presente y donde podremos seleccionar dónde queremos ir
        - el Fragment principal (nav_host_fragment_activity_main), donde se visualizarán el resto de fragments
         */

        // referenciamos ambos de activity_main.xml
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // configuracion de la barra superior y los fragments por los que navegará la app (todos en "mobile_navitagion.xml)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_gallinas,
                R.id.navigation_huevos,
                R.id.navigation_estancias,
                R.id.navigation_estadisticas
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // para que el botón de retroceso (símbolo de flecha) en la app bar funcione correctamente
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}