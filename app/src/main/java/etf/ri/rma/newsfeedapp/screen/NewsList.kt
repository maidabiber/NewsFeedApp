package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.model.NewsItem

@Composable
fun NewsList(listaVijesti: List<NewsItem>,stanje: LazyListState, kat: String) {
    LazyColumn(Modifier.testTag("news_list"),
        contentPadding = PaddingValues(all = 8.dp),
        state = stanje

    ) {
        if (listaVijesti.isEmpty()==false) {
            items( listaVijesti, { it.id }) {
                trenutnaVijest ->
                if (trenutnaVijest.isFeatured==false)
                    StandardNewsCard(nasaTrenutnaVijest = trenutnaVijest)
                else
                    FeaturedNewsCard(nasaTrenutnaVijest = trenutnaVijest)
            }
        } else {
            item {
                MessageCard("Nema pronađenih vijesti u kategoriji $kat")
            }


        }
    }
}