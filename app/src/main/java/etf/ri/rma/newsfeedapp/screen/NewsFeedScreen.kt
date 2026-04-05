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
    selectedCategory: String = "Sve",
    dateRange: Pair<String, String>? = null,
    onCategoryChanged: (String) -> Unit = {},
    unwantedWords: List<String> = emptyList(),
    newsDao: NewsDAO,
    savedNewsRepository: SavedNewsRepository
) {
    val backgroundColor = Color(0xFFD3D3D3)
    val dateFormatter = remember {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var displayedNews by remember { mutableStateOf(emptyList<NewsItem>()) }
    var loading by remember { mutableStateOf(true) }

    val currState = rememberLazyListState()

    LaunchedEffect(selectedCategory, dateRange, unwantedWords) {
        loading = true
        errorMessage = null

        try {
            newsDao.populateWithInitialNews()

            if (selectedCategory != "Sve") {
                val newStories = newsDao.getTopStoriesByCategory(selectedCategory, true)
                for (story in newStories) {
                    savedNewsRepository.saveNews(story)
                    story.imageUrl?.let { url ->
                        try {
                            val tags = ImagaDAO().getTags(url)
                            val id = savedNewsRepository.getNewsIdByUuid(story.uuid)
                           if (id!=null) savedNewsRepository.addTagsToNews(tags, id)
                        } catch (_: Exception) { }
                    }
                }
            }



            val allCashedNews = savedNewsRepository.getAllNews()

            val filteredNews = allCashedNews.filter { newsItem ->
                val isCorrectCategory = if (selectedCategory == "Sve") {
                    true
                } else {
                    val targetNewsItemApiCategory = newsDao.getNewsItemApiCategory(selectedCategory)
                    newsItem.category.equals(targetNewsItemApiCategory, ignoreCase = true)
                }

                val matchesDateRange = if (dateRange != null) {
                    val (startDateStr, endDateStr) = dateRange
                    try {
                        val newsDate = dateFormatter.parse(newsItem.publishedDate)
                        val startDate = dateFormatter.parse(startDateStr)
                        val endDate = dateFormatter.parse(endDateStr)

                        newsDate != null && !newsDate.before(startDate) && !newsDate.after(endDate)
                    } catch (e: Exception) {
                        false
                    }
                } else true

                val matchesUnwantedWords = if (unwantedWords.isNotEmpty()) {
                    val combinedText = "${newsItem.title.orEmpty()} ${newsItem.snippet.orEmpty()}".lowercase(Locale.ROOT)
                    unwantedWords.none { unwantedWord ->
                        combinedText.contains(unwantedWord.lowercase(Locale.ROOT))
                    }
                } else true

                isCorrectCategory && matchesDateRange && matchesUnwantedWords
            }

            val sortedNews = filteredNews.sortedWith { a, b ->
                val aIsFeatured = a.isFeatured
                val bIsFeatured = b.isFeatured

                when {
                    aIsFeatured && !bIsFeatured -> -1
                    !aIsFeatured && bIsFeatured -> 1
                    else -> {
                        try {
                            val dateA = dateFormatter.parse(a.publishedDate) ?: Date(0)
                            val dateB = dateFormatter.parse(b.publishedDate) ?: Date(0)
                            dateB.compareTo(dateA)
                        } catch (GRESKA: Exception) {
                            0
                        }
                    }
                }
            }

            displayedNews = sortedNews

        } catch (e: Exception) {
            errorMessage = "Došlo je do greške pri učitavanju vijesti: ${e.localizedMessage ?: "Nepoznata greška"}"
            displayedNews = emptyList()
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

    val currentActiveIndex = remember(selectedCategory) {
        dugmici.indexOfFirst { it.first.equals(selectedCategory, ignoreCase = true) }
            .coerceAtLeast(0)
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(title = { Text("Vijesti", fontWeight = FontWeight.Bold) },
                modifier = Modifier.background(backgroundColor).statusBarsPadding(),
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = backgroundColor
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
                    val vrijednost = currentActiveIndex == indeks
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
                errorMessage != null -> {
                    Text("Error: $errorMessage", color = Color.Red, modifier = Modifier.padding(16.dp))
                }
                displayedNews.isEmpty() -> {
                    Text("Nema dostupnih vijesti.", modifier = Modifier.padding(16.dp))
                }
                else -> {
                    NewsList(displayedNews, currState, navController, selectedCategory)
                }
            }
        }
    }
}
