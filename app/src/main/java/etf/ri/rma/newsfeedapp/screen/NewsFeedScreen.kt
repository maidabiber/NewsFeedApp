package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import etf.ri.rma.newsfeedapp.data.dao.SavedNewsRepository
import etf.ri.rma.newsfeedapp.data.dao.NewsDAO
import etf.ri.rma.newsfeedapp.data.dao.ImagaDAO
import etf.ri.rma.newsfeedapp.model.NewsItem

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    navController: NavController = rememberNavController(),
    kategorijaKojuSmoIzabrali: String = "Sve",
    opsegDatuma: Pair<String, String>? = null,
    onCategoryChanged: (String) -> Unit = {},
    listaNezeljenihRijeci: List<String> = emptyList(),
    newsDao: NewsDAO,
    savedNewsRepository: SavedNewsRepository
) {
    val pozadinskaBoja = Color(0xFFD3D3D3)
    val dateFormatter = remember {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    var greska by remember { mutableStateOf<String?>(null) }
    var vijestiKojeSuPrikazane by remember { mutableStateOf(emptyList<NewsItem>()) }
    var loading by remember { mutableStateOf(true) }

    val stanjeTrenutno = rememberLazyListState()

    LaunchedEffect(kategorijaKojuSmoIzabrali, opsegDatuma, listaNezeljenihRijeci) {
        loading = true
        greska = null

        try {
            newsDao.popuniSaPocetnimVijestima()

            if (kategorijaKojuSmoIzabrali != "Sve") {
                val noveVijesti = newsDao.getTopStoriesByCategory(kategorijaKojuSmoIzabrali, true)
                for (vijest in noveVijesti) {
                    savedNewsRepository.sacuvajVijest(vijest)
                    vijest.imageUrl?.let { url ->
                        try {
                            val tagovi = ImagaDAO().getTags(url)
                            val id = savedNewsRepository.dohvatiIdVijestiPoUuid(vijest.uuid)
                           if (id!=null) savedNewsRepository.dodajTagoveZaVijest(tagovi, id)
                        } catch (_: Exception) { }
                    }
                }
            }

            val sveVijestiIzKesa = savedNewsRepository.sveVijesti()

            val filtriraneVijesti = sveVijestiIzKesa.filter { newsItem ->
                val odgovarajucaKat = if (kategorijaKojuSmoIzabrali == "Sve") {
                    true
                } else {
                    val targetNewsItemApiCategory = newsDao.getNewsItemApiCategory(kategorijaKojuSmoIzabrali)
                    newsItem.category.equals(targetNewsItemApiCategory, ignoreCase = true)
                }

                val matchesDateRange = if (opsegDatuma != null) {
                    val (startDateStr, endDateStr) = opsegDatuma
                    try {
                        val datumVijesti = dateFormatter.parse(newsItem.publishedDate)
                        val pocetniDatum = dateFormatter.parse(startDateStr)
                        val krajnjiDatum = dateFormatter.parse(endDateStr)

                        datumVijesti != null && !datumVijesti.before(pocetniDatum) && !datumVijesti.after(krajnjiDatum)
                    } catch (e: Exception) {
                        false
                    }
                } else true

                val matchesUnwantedWords = if (listaNezeljenihRijeci.isNotEmpty()) {
                    val combinedText = "${newsItem.title.orEmpty()} ${newsItem.snippet.orEmpty()}".lowercase(Locale.ROOT)
                    listaNezeljenihRijeci.none { unwantedWord ->
                        combinedText.contains(unwantedWord.lowercase(Locale.ROOT))
                    }
                } else true

                odgovarajucaKat && matchesDateRange && matchesUnwantedWords
            }

            val sortiraneVijesti = filtriraneVijesti.sortedWith { a, b ->
                val aIsFeatured = a.isFeatured
                val bIsFeatured = b.isFeatured

                when {
                    aIsFeatured && !bIsFeatured -> -1
                    !aIsFeatured && bIsFeatured -> 1
                    else -> {
                        try {
                            val datumA = dateFormatter.parse(a.publishedDate) ?: Date(0)
                            val datumB = dateFormatter.parse(b.publishedDate) ?: Date(0)
                            datumB.compareTo(datumA)
                        } catch (GRESKA: Exception) {
                            0
                        }
                    }
                }
            }

            vijestiKojeSuPrikazane = sortiraneVijesti

        } catch (e: Exception) {
            greska = "Došlo je do greške pri učitavanju vijesti: ${e.localizedMessage ?: "Nepoznata greška"}"
            vijestiKojeSuPrikazane = emptyList()
        } finally {
            loading = false
        }
    }

    val dugmici = listOf(
        Pair("Sve", "filter_chip_all"),
        Pair("Politika", "filter_chip_pol"),
        Pair("Sport", "filter_chip_spo"),
        Pair("Umjetnost", "filter_chip_art"),
        Pair("Nauka", "filter_chip_sci"),
        Pair("Tehnologija", "filter_chip_tech"),
        Pair("Više filtera ...", "filter_chip_more")
    )

    val trenutnoAktivna = remember(kategorijaKojuSmoIzabrali) {
        dugmici.indexOfFirst { it.first.equals(kategorijaKojuSmoIzabrali, ignoreCase = true) }
            .coerceAtLeast(0)
    }

    Scaffold(
        containerColor = pozadinskaBoja,
        topBar = {
            TopAppBar(title = { Text("Vijesti", fontWeight = FontWeight.Bold) },
                modifier = Modifier.background(pozadinskaBoja).statusBarsPadding(),
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = pozadinskaBoja
                )
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dugmici.forEachIndexed { indeks, (naziv, tag) ->
                    val vrijednost = trenutnoAktivna == indeks
                    FilterChip(
                        colors = FilterChipDefaults.filterChipColors(
                            selectedLabelColor = Color.White,
                            selectedContainerColor = Color(90, 90, 160),
                            containerColor = Color(0xFFE6E6FA),
                            labelColor = Color.Black,
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
                        modifier = Modifier.testTag(tag),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(9.dp)
                    )
                    Text("Učitavanje...", modifier = Modifier.padding(10.dp))
                }
                greska != null -> {
                    Text("Error: $greska", color = Color.Red, modifier = Modifier.padding(16.dp))
                }
                vijestiKojeSuPrikazane.isEmpty() -> {
                    Text("Nema dostupnih vijesti.", modifier = Modifier.padding(16.dp))
                }
                else -> {
                    NewsList(vijestiKojeSuPrikazane, stanjeTrenutno, navController, kategorijaKojuSmoIzabrali)
                }
            }
        }
    }
}
