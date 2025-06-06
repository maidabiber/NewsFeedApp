package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.model.NewsItem


@Composable
fun NewsList(listaVijesti: List<NewsItem>, stanje: LazyListState, navController: NavController, categoryFilter: String) {
    LazyColumn(Modifier.testTag("news_list"),
        contentPadding = PaddingValues(all = 8.dp),
        state = stanje
    ) {
        if (listaVijesti.isNotEmpty()) {
            items( listaVijesti, { it.uuid }) { // Koristimo uuid kao ključ
                    trenutnaVijest ->
                if (trenutnaVijest.isFeatured) {
                    FeaturedNewsCard(nasaTrenutnaVijest = trenutnaVijest) { newsUuid ->
                        navController.navigate("/details/$newsUuid")
                    }
                } else {
                    StandardNewsCard(nasaTrenutnaVijest = trenutnaVijest) { newsUuid ->
                        navController.navigate("/details/$newsUuid")
                    }
                }
            }
        } else {
            item {
                MessageCard("Nema pronađenih vijesti u kategoriji $categoryFilter")
            }
        }
    }
}
