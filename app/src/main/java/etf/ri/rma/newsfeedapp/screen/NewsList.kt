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
fun NewsList(listaVijesti: List<NewsItem>,stanje: LazyListState, kat: String, navController: NavController) {
    LazyColumn(Modifier.testTag("news_list"),
        contentPadding = PaddingValues(all = 8.dp),
        state = stanje
    ) {
        if (listaVijesti.isEmpty()==false) {
            items( listaVijesti, { it.id }) {
                    trenutnaVijest ->
                if (trenutnaVijest.isFeatured==false)
                {
                    StandardNewsCard(nasaTrenutnaVijest = trenutnaVijest, onClick = { newsId -> navController.navigate("/details/$newsId") }
                    )

                }
                else
                {
                    FeaturedNewsCard(nasaTrenutnaVijest = trenutnaVijest) { newsId ->
                        navController.navigate("/details/$newsId")
                    }
                }
            }
        } else {
            item {
                MessageCard("Nema pronađenih vijesti u kategoriji $kat")
            }
        }
    }
}
