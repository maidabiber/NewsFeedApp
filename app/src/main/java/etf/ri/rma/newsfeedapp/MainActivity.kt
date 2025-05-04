package etf.ri.rma.newsfeedapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import etf.ri.rma.newsfeedapp.screen.FilterScreen
import etf.ri.rma.newsfeedapp.screen.NewsFeedScreen
import etf.ri.rma.newsfeedapp.ui.theme.NewsFeedAppTheme
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import etf.rma.newsfeedapp.screen.NewsDetailsScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsFeedAppTheme {
                var nepozeljneRijeci by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
                var izabraniOpsegDatuma by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }
                var rawDateRange by rememberSaveable { mutableStateOf<Pair<Long, Long>?>(null) }
                val navController = rememberNavController()
                var formattedDateRange by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }
                var odabranaKategorija by rememberSaveable { mutableStateOf("Sve") }

                Surface(color = Color(0xFF, 0xD3, 0xD3, 0xD3),
                    modifier = Modifier.fillMaxSize()
                ) {

                    NavGraph(navController = navController, odabranaKategorija = odabranaKategorija, nepozeljneRijeci = nepozeljneRijeci,
                        onCategoryChanged = { odabranaKategorija = it }, onDateRangeChanged = { izabraniOpsegDatuma = it },
                        rasponDatuma = izabraniOpsegDatuma,
                        onUnwantedWordsChanged = { nepozeljneRijeci = it }
                    )
                }
            }
        }
    }
}

@Composable
fun NavGraph(navController: NavHostController, onDateRangeChanged: (Pair<String, String>?) -> Unit,
    odabranaKategorija: String, rasponDatuma: Pair<String, String>?,
     onCategoryChanged: (String) -> Unit,
    onUnwantedWordsChanged: (List<String>) -> Unit,
             nepozeljneRijeci: List<String>
) {

    NavHost(navController = navController, startDestination = "/home") {
        composable("/home") {
            NewsFeedScreen(navController = navController,
                opsegDatuma = rasponDatuma,
                kategorijaKojuSmoIzabrali = odabranaKategorija,  listaNezeljenihRijeci = nepozeljneRijeci, onCategoryChanged = onCategoryChanged
            )
        }
        composable("/filters") {
            FilterScreen(
                navController = navController, opsegDatuma = rasponDatuma,onDateRangeChanged = onDateRangeChanged,
                nezeljeneRijeci = nepozeljneRijeci,
                kategorijaKojuSmoIzabrali = odabranaKategorija,
                onCategoryChanged = onCategoryChanged,
                onUnwantedWordsChanged = onUnwantedWordsChanged
            )
        }
        composable(route = "/details/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val idVijesti = backStackEntry.arguments?.getString("id")
            if (idVijesti != null) {
                NewsDetailsScreen(navController = navController, newsId = idVijesti)
            }
        }
    }
}
