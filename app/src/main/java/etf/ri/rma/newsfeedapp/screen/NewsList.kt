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
fun NewsList(newsList: List<NewsItem>, listState: LazyListState, navController: NavController, categoryFilter: String) {
    LazyColumn(Modifier.testTag("news_list"),
        contentPadding = PaddingValues(all = 8.dp),
        state = listState
    ) {
        if (newsList.isNotEmpty()) {
            items( newsList, { it.uuid }) { // Koristimo uuid kao ključ
                    currentNews ->
                if (currentNews.isFeatured) {
                    FeaturedNewsCard(newsItem = currentNews) { newsUuid ->
                        navController.navigate("/details/$newsUuid")
                    }
                } else {
                    StandardNewsCard(newsItem = currentNews) { newsUuid ->
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
