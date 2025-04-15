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
fun NewsList(listaVijesti: List<NewsItem>, stanje: LazyListState, kat: String,
             vijestiNaKojeSamKliknula: Map<String, Boolean>,
             onNewsClick: (String) -> Unit
) {
    LazyColumn(
        Modifier.testTag("news_list"),
        contentPadding = PaddingValues(all = 8.dp), state= stanje
    ) {
        if (!listaVijesti.isEmpty()) {
            items(listaVijesti, key = { it.id }) { trenutnaVijest ->
                val jeLiKlikIzvrsen = vijestiNaKojeSamKliknula[trenutnaVijest.id] ?: false
                if (!trenutnaVijest.isFeatured) {
                    StandardNewsCard(nasaTrenutnaVijest = trenutnaVijest, onClick = { onNewsClick(trenutnaVijest.id) },

                        izvrsenKlik = jeLiKlikIzvrsen
                    )
                } else {
                    FeaturedNewsCard(
                        nasaTrenutnaVijest = trenutnaVijest,
                        onClick = { onNewsClick(trenutnaVijest.id) },
                        jeLiKlikIzvrsen = jeLiKlikIzvrsen
                    )
                }
            }
        } else {
            item {
                MessageCard("Nema pronađenih vijesti u kategoriji $kat")
            }
        }
    }
}

