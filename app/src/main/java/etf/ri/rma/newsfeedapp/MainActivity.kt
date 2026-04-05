
package etf.ri.rma.newsfeedapp

import android.os.Bundle
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
import etf.ri.rma.newsfeedapp.data.dao.SavedNewsRepository
import etf.ri.rma.newsfeedapp.data.db.NewsDatabase
import etf.ri.rma.newsfeedapp.data.dao.NewsDAO
import etf.ri.rma.newsfeedapp.screen.NewsFeedScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsFeedAppTheme {
                var unwantedWords by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
                var selectedDateRange by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }
                var selectedCategory by rememberSaveable { mutableStateOf("Sve") }

               val sharedNewsDao = remember { NewsDAO() }
                val database = NewsDatabase.getInstance(applicationContext)
                val savedNewsRepository = SavedNewsRepository(database.savedNewsDAO())
                Surface(
                    color = Color(0xFFD3D3D3),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()

                    NavGraph(
                        navController = navController,
                        selectedCategory = selectedCategory,
                        unwantedWords = unwantedWords,
                        onCategoryChanged = { selectedCategory = it },
                        onDateRangeChanged = { selectedDateRange = it },
                        dateRange = selectedDateRange,
                        onUnwantedWordsChanged = { unwantedWords = it },
                        newsDao = sharedNewsDao,
                        savedNewsRepository = savedNewsRepository
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
    selectedCategory: String,
    dateRange: Pair<String, String>?,
    onCategoryChanged: (String) -> Unit,
    onUnwantedWordsChanged: (List<String>) -> Unit,
    unwantedWords: List<String>,
    newsDao: NewsDAO,
    savedNewsRepository: SavedNewsRepository
) {

    NavHost(navController = navController, startDestination = "/home") {
        composable("/home") {
            NewsFeedScreen(
                navController = navController,
                dateRange = dateRange,
                selectedCategory = selectedCategory,
                unwantedWords = unwantedWords,
                onCategoryChanged = onCategoryChanged,
                newsDao = newsDao,
                savedNewsRepository = savedNewsRepository
            )
        }
        composable("/filters") {
            FilterScreen(
                navController = navController, selectedDateRange = dateRange,onDateRangeChanged = onDateRangeChanged,
                unwantedWords = unwantedWords,
                selectedCategory = selectedCategory,
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
                NewsDetailsScreen(newsId = newsId, navController = navController, newsDao = newsDao, savedNewsRepository=savedNewsRepository)// savedNewsRepository = savedNewsRepository)
            } else {
                Text("Greška: ID vijesti nije dostupan.")
            }
        }
    }
}
