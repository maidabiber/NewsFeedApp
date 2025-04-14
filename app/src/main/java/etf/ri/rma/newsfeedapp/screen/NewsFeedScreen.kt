package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.data.NewsData

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewsFeedScreen() {
    val sveVijesti = remember { NewsData.getAllNews() }

    val dugmici = listOf(
        Pair("Sve", "filter_chip_all"), Pair("Politika", "filter_chip_pol"), Pair("Sport", "filter_chip_spo"), Pair("Umjetnost", "filter_chip_none"), Pair("Nauka / tehnologija", "filter_chip_sci")
    )

    var trenutnoAktivna by rememberSaveable { mutableStateOf(0) }

    val stanjeTrenutno = rememberLazyListState()
    LaunchedEffect(trenutnoAktivna) {
        stanjeTrenutno.animateScrollToItem(0)
    }
    val filtrirane = remember(trenutnoAktivna, sveVijesti) {
        if (trenutnoAktivna == 0) {sveVijesti}
        else sveVijesti.filter {
            it.category.equals(dugmici[trenutnoAktivna].first, ignoreCase = true)
        }
    }
    Column {
        Spacer(Modifier.height(33.dp))
        FlowRow(Modifier.padding(17.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)

        ) {
            dugmici.forEachIndexed { indeks, (naziv, tag) ->
               val vrijednost=trenutnoAktivna == indeks
                FilterChip(
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,

                        containerColor = if (vrijednost==true) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.99f)
                        } else { MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)},

                        labelColor = if (vrijednost==true) {
                            MaterialTheme.colorScheme.onPrimaryContainer}
                        else {
                            MaterialTheme.colorScheme.onSurfaceVariant},

                    ),
                    selected = vrijednost,
                    onClick = { trenutnoAktivna = indeks },
                    label = { Text(naziv) },
                    modifier = Modifier.padding(horizontal = 8.dp).testTag(tag),


                )
            }
        }
        Spacer(Modifier.height(2.dp))
        NewsList(filtrirane, stanjeTrenutno, dugmici[trenutnoAktivna].first)
    }
}
