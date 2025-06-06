
package etf.ri.rma.newsfeedapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import etf.ri.rma.newsfeedapp.screen.FilterScreen
import etf.ri.rma.newsfeedapp.ui.theme.NewsFeedAppTheme
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import etf.ri.rma.newsfeedapp.screen.NewsDetailsScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import etf.ri.rma.newsfeedapp.data.network.NewsDAO

import etf.ri.rma.newsfeedapp.screen.NewsFeedScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsFeedAppTheme {
                Log.d("TEST_LOG", "MainActivity onCreate pozvan!")
                var nepozeljneRijeci by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
                var izabraniOpsegDatuma by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }
                var odabranaKategorija by rememberSaveable { mutableStateOf("Sve") }

               val sharedNewsDao = remember { NewsDAO() }

                Surface(
                    color = Color(0xFFD3D3D3),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()

                    NavGraph(
                        navController = navController,
                        odabranaKategorija = odabranaKategorija,
                        nepozeljneRijeci = nepozeljneRijeci,
                        onCategoryChanged = { odabranaKategorija = it },
                        onDateRangeChanged = { izabraniOpsegDatuma = it },
                        rasponDatuma = izabraniOpsegDatuma,
                        onUnwantedWordsChanged = { nepozeljneRijeci = it },
                        newsDao = sharedNewsDao
                    )
                }
            }
        }
    }
}



@Composable
fun NavGraph(
    navController: NavHostController,
    onDateRangeChanged: (Pair<String, String>?) -> Unit,
    odabranaKategorija: String,
    rasponDatuma: Pair<String, String>?,
    onCategoryChanged: (String) -> Unit,
    onUnwantedWordsChanged: (List<String>) -> Unit,
    nepozeljneRijeci: List<String>,
    newsDao: NewsDAO
) {

    NavHost(navController = navController, startDestination = "/home") {
        composable("/home") {
            NewsFeedScreen(
                navController = navController,
                opsegDatuma = rasponDatuma,
                kategorijaKojuSmoIzabrali = odabranaKategorija,
                listaNezeljenihRijeci = nepozeljneRijeci,
                onCategoryChanged = onCategoryChanged,
                newsDao = newsDao
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
        composable(
            route = "/details/{newsId}",
            arguments = listOf(navArgument("newsId") { type = NavType.StringType })
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId")
            if (newsId != null) {
                NewsDetailsScreen(newsId = newsId, navController = navController, newsDao = newsDao)
            } else {
                Text("Greška: ID vijesti nije dostupan.")
            }
        }
    }
}
