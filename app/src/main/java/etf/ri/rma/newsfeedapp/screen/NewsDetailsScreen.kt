package etf.ri.rma.newsfeedapp.screen

import android.Manifest
import android.net.NetworkCapabilities
import etf.ri.rma.newsfeedapp.data.dao.NewsDAO
import etf.ri.rma.newsfeedapp.data.dao.ImagaDAO

import etf.ri.rma.newsfeedapp.model.NewsItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.RequiresPermission

import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import etf.ri.rma.newsfeedapp.R
import etf.ri.rma.newsfeedapp.data.dao.SavedNewsRepository

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailsScreen(
    newsId: String,
    navController: NavController,
    newsDao: NewsDAO,
    savedNewsRepository: SavedNewsRepository
) {
    var selectedNews by remember { mutableStateOf<NewsItem?>(null) }
    val imgDao = remember { ImagaDAO() }
    var isNewsSaved by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isDetailsLoading by remember { mutableStateOf(true) }
    var apiImageTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var cachedNewsTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var similarNewsList by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    val context = LocalContext.current
    var online by remember { mutableStateOf(false) }
    var displayTags by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(newsId) {
        isDetailsLoading = true
        errorMessage = null
        online = isNetworkAvailable(context)

        var newsIdFromDb: Int? = null

        try {
            newsIdFromDb = savedNewsRepository.getNewsIdByUuid(newsId)
            if (newsIdFromDb != null) {
                val newsWithTags = savedNewsRepository.getNewsWithTag(newsIdFromDb)
                newsWithTags?.let {
                    selectedNews = NewsItem(
                        id = it.news.id,
                        snippet = it.news.snippet,
                        title = it.news.title,
                        source = it.news.source,
                        imageUrl = it.news.imageUrl,
                        uuid = it.news.uuid,
                        category = it.news.category,
                        isFeatured = it.news.isFeatured,
                        publishedDate = it.news.publishedDate,
                        imageTags = it.tags
                    )
                    cachedNewsTags = it.tags.map { it.value }
                    displayTags = cachedNewsTags
                    isNewsSaved = true
                }
            }
        } catch (e: Exception) {
            errorMessage = "Greška pri dohvatanju iz baze: ${e.message}"
        }

        if (selectedNews == null) {
            try {
                val vijest = newsDao.getNewsItem(newsId)
                selectedNews = vijest
                isNewsSaved = false
            } catch (e: Exception) {
                errorMessage = "Greška pri dohvatanju vijesti s weba: ${e.message}"
            }
        }

        selectedNews?.let { vijest ->
            if (!vijest.imageUrl.isNullOrBlank()) {
                if (cachedNewsTags.isNotEmpty()) {
                    displayTags = cachedNewsTags
                } else if (online) {
                    try {
                        val tags = imgDao.getTags(vijest.imageUrl)
                        displayTags = tags
                        apiImageTags = tags
                        val isSaved = savedNewsRepository.saveNews(vijest)
                        if (isSaved) {
                            val id = savedNewsRepository.getNewsIdByUuid(vijest.uuid)
                            if (id != null) {
                                savedNewsRepository.addTagsToNews(tags, id)
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Greška pri dohvatanju tagova s API-ja: ${e.message}"
                    }
                } else {
                    errorMessage = "Nema interneta i tagovi nisu dostupni u bazi."
                }
            }
        }

        selectedNews?.let { currentNews ->
            similarNewsList = if (online) {
                newsDao.getSimilarStories(currentNews.uuid).filter { it.uuid != currentNews.uuid }
            } else if (displayTags.isNotEmpty()) {
                savedNewsRepository.getSimilarNews(displayTags.take(2)).filter { it.uuid != currentNews.uuid }
            } else {
                emptyList()
            }
        }

        isDetailsLoading = false
    }
    val pozadinskaBoja = Color(0xFFD3D3D3)
    val plavaKartica = Color(0xFFE6E6FA)

    Scaffold(
        containerColor = pozadinskaBoja,
        topBar = {
            TopAppBar(
                title = { Text("Detalji vijesti", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        val popped = navController.popBackStack("/home", inclusive = false)
                        if (!popped) {
                            navController.navigate("/home") {
                                popUpTo("/home") { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Nazad")
                    }
                },
                modifier = Modifier
                    .background(pozadinskaBoja)
                    .statusBarsPadding(),
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = pozadinskaBoja)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("details_close_button"),
                    onClick = { navController.popBackStack("/home", inclusive = false) }
                ) {
                    Text("Zatvori detalje")
                }
            }
        }
    ) { paddingValues ->
        if (isDetailsLoading && selectedNews == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ucitavamo detalje vijesti...", modifier = Modifier.padding(top = 8.dp))
            }
        } else if (selectedNews != null) {
            Column(
                modifier = Modifier.padding(paddingValues).padding(16.dp).shadow(12.dp, shape = MaterialTheme.shapes.medium).background(plavaKartica, shape = MaterialTheme.shapes.medium).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!selectedNews!!.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(selectedNews!!.imageUrl).crossfade(true).build(),
                        placeholder = painterResource(R.drawable.bosnjaci),
                        error = painterResource(R.drawable.bosnjaci),
                        contentDescription = "Naslovna slika vijesti",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.bosnjaci),
                        contentDescription = "Zamjenska slika",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    color = Color(0xFF2C3E50),
                    text = selectedNews!!.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("details_title").padding(top = 16.dp).padding(bottom = 8.dp)
                )
                Text(
                    text = selectedNews!!.snippet,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("details_snippet")
                )
                Spacer(Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Kategorija: ${selectedNews!!.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.testTag("details_category").background(Color(90, 90, 160), shape = MaterialTheme.shapes.small).padding(horizontal = 5.dp, vertical = 5.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Izvor: ${selectedNews!!.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.testTag("details_source").background(Color(90, 90, 160), shape = MaterialTheme.shapes.small).padding(horizontal = 5.dp, vertical = 5.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Datum objave: ${selectedNews!!.publishedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.testTag("details_date").background(Color(90, 90, 160), shape = MaterialTheme.shapes.small).padding(horizontal = 5.dp, vertical = 5.dp)
                    )
                    if (apiImageTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tagovi slike (Imagga API):",
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        apiImageTags.forEach { tag ->
                            Text(text = "• $tag", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else if (!selectedNews!!.imageUrl.isNullOrBlank()) {
                        Text(text = "", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (displayTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tagovi slike:",
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        displayTags.forEach { tag ->
                            Text(text = "• $tag", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else if (!selectedNews!!.imageUrl.isNullOrBlank()) {
                        Text(text = "Nema dostupnih tagova za ovu vijest.", style = MaterialTheme.typography.bodyMedium)
                    }


                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Povezane vijesti:",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                relatedNews(relatedNewsList = similarNewsList, navController = navController)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Vijest nije pronadjena ili je doslo do greske pri ucitavanju.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Gray,
                    modifier = Modifier.testTag("news_not_found_message")
                )
                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun relatedNews(relatedNewsList: List<NewsItem>, navController: NavController) {
    BackHandler {
        navController.popBackStack("/home", inclusive = false)
    }
    Column(modifier = Modifier.testTag("related_news_list"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (relatedNewsList.isNotEmpty()) {
            relatedNewsList.forEachIndexed { index, vijest ->
                Text(
                    text = vijest.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        shadow = Shadow(color = Color.Black.copy(alpha = 0.2f), offset = Offset(2f, 2f))
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { navController.navigate("/details/${vijest.uuid}") }
                        .testTag(if (index == 0) "related_news_title_1" else "related_news_title_2")
                )
            }
        } else {
            Text(text = "Nema pronađenih sličnih vijesti u bazi (po tagovima).", style = MaterialTheme.typography.bodyMedium)
        }
    }
}