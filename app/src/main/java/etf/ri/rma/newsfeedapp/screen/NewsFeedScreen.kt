package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import etf.ri.rma.newsfeedapp.data.NewsData
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewsFeedScreen(navController: NavController = rememberNavController(),
    kategorijaKojuSmoIzabrali: String = "", opsegDatuma: Pair<String, String>? = null,
    onCategoryChanged: (String) -> Unit = {}, listaNezeljenihRijeci: List<String> = emptyList(),
) {
    val sveVijesti = remember { NewsData.getAllNews() }
    val stanjeTrenutno = rememberLazyListState()
    val formatiranjeDatuma = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    val filtriraneVijesti by remember(sveVijesti, kategorijaKojuSmoIzabrali, opsegDatuma, listaNezeljenihRijeci) {
        derivedStateOf {
            var privremeneVijesti = sveVijesti

            if (kategorijaKojuSmoIzabrali != "Sve") {
                privremeneVijesti = privremeneVijesti.filter { it.category.equals(kategorijaKojuSmoIzabrali, ignoreCase = true) }
            }

            if (opsegDatuma != null) {
                val (startDateStr, endDateStr) = opsegDatuma
                val pocetniDatum = formatiranjeDatuma.parse(startDateStr)?.time
                val krajnjiDatum = formatiranjeDatuma.parse(endDateStr)?.time

                if (pocetniDatum != null && krajnjiDatum != null) {
                    privremeneVijesti = privremeneVijesti.filter { vijest ->
                        val datumVijesti = formatiranjeDatuma.parse(vijest.publishedDate)?.time
                        datumVijesti != null && datumVijesti in pocetniDatum..krajnjiDatum
                    }
                }
            }


            if (listaNezeljenihRijeci.isEmpty()==false) {
                privremeneVijesti = privremeneVijesti.filter { vijest ->
                    listaNezeljenihRijeci.none { nepozeljnaRijec ->
                        vijest.title.contains(nepozeljnaRijec, ignoreCase = true) ||
                                vijest.snippet.contains(nepozeljnaRijec, ignoreCase = true)
                    }
                }
            }

            privremeneVijesti
        }
    }

    LaunchedEffect(kategorijaKojuSmoIzabrali, opsegDatuma, listaNezeljenihRijeci) {
        stanjeTrenutno.animateScrollToItem(0)
    }

    val dugmici = listOf(Pair("Sve", "filter_chip_all"),
        Pair("Politika", "filter_chip_pol"),
        Pair("Sport", "filter_chip_spo"),
        Pair("Umjetnost", "filter_chip_none"),
        Pair("Nauka / tehnologija", "filter_chip_sci"), Pair("Više filtera ...", "filter_chip_more")
    )
    val trenutnoAktivna = remember(kategorijaKojuSmoIzabrali) {
        dugmici.indexOfFirst { it.first.equals(kategorijaKojuSmoIzabrali, ignoreCase = true) }.coerceAtLeast(0)
    }


    Column {
        Spacer(Modifier.height(33.dp))
        FlowRow(
            Modifier.padding(17.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            dugmici.forEachIndexed { indeks, (naziv, tag) ->
                val vrijednost = trenutnoAktivna == indeks
                FilterChip(
                    colors = FilterChipDefaults.filterChipColors(
                        selectedLabelColor = Color.White,
                        selectedContainerColor = Color(90, 90, 160),
                        containerColor =Color(0xFFE6E6FA),
                        labelColor =
                            Color.Black,
                    ),
                    selected = vrijednost,
                    onClick = {
                        if (indeks != dugmici.lastIndex) {
                           onCategoryChanged(naziv)

                        } else {
                            navController.navigate("/filters")
                        }
                    },
                    label = { Text(naziv) },
                    modifier = Modifier.padding(horizontal = 8.dp).testTag(tag),
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        NewsList(filtriraneVijesti, stanjeTrenutno, kategorijaKojuSmoIzabrali, navController)
    }
}