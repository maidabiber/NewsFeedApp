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

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import etf.ri.rma.newsfeedapp.R
import etf.ri.rma.newsfeedapp.data.dao.SavedNewsRepository
import kotlinx.coroutines.launch

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun imaLiInterneta(context: Context): Boolean {
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
    var odabranaVijest by remember { mutableStateOf<NewsItem?>(null) }
    val imgDao = remember { ImagaDAO() }
    var jeLiVijestSacuvana by remember { mutableStateOf(false) }
    var porukaGreske by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var ucitavanjeDetaljaUToku by remember { mutableStateOf(true) }
    var tagoviSlikeIzApi by remember { mutableStateOf<List<String>>(emptyList()) }
    var tagoviVijestiIzBaze by remember { mutableStateOf<List<String>>(emptyList()) }
    var vijestiKojeSuSlicne by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    val context = LocalContext.current
    var online by remember { mutableStateOf(false) }
    var tagoviZaPrikaz by remember { mutableStateOf<List<String>>(emptyList()) }
    var tagoviApi by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(newsId) {
        ucitavanjeDetaljaUToku = true
        porukaGreske = null
        online = imaLiInterneta(context)

        var idVijestiIzBaze: Int? = null

        try {
            idVijestiIzBaze = savedNewsRepository.dohvatiIdVijestiPoUuid(newsId)
            if (idVijestiIzBaze != null) {
                val vijestSaTagovima = savedNewsRepository.dohvatiVijestSaTagovima(idVijestiIzBaze)
                vijestSaTagovima?.let {
                    odabranaVijest = NewsItem(
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
                    tagoviVijestiIzBaze = it.tags.map { it.value }
                    tagoviZaPrikaz = tagoviVijestiIzBaze
                    jeLiVijestSacuvana = true
                }
            }
        } catch (e: Exception) {
            porukaGreske = "Greška pri dohvatanju iz baze: ${e.message}"
        }

        if (odabranaVijest == null) {
            try {
                val vijest = newsDao.getNewsItem(newsId)
                odabranaVijest = vijest
                jeLiVijestSacuvana = false
            } catch (e: Exception) {
                porukaGreske = "Greška pri dohvatanju vijesti s weba: ${e.message}"
            }
        }

        odabranaVijest?.let { vijest ->
            if (!vijest.imageUrl.isNullOrBlank()) {
                if (tagoviVijestiIzBaze.isNotEmpty()) {
                    tagoviZaPrikaz = tagoviVijestiIzBaze
                } else if (online) {
                    try {
                        val tagovi = imgDao.getTags(vijest.imageUrl)
                        tagoviZaPrikaz = tagovi
                        tagoviSlikeIzApi = tagovi
                        val sacuvano = savedNewsRepository.sacuvajVijest(vijest)
                        if (sacuvano) {
                            val id = savedNewsRepository.dohvatiIdVijestiPoUuid(vijest.uuid)
                            if (id != null) {
                                savedNewsRepository.dodajTagoveZaVijest(tagovi, id)
                            }
                        }
                    } catch (e: Exception) {
                        porukaGreske = "Greška pri dohvatanju tagova s API-ja: ${e.message}"
                    }
                } else {
                    porukaGreske = "Nema interneta i tagovi nisu dostupni u bazi."
                }
            }
        }

        odabranaVijest?.let { trenutnaVijest ->
            vijestiKojeSuSlicne = if (online) {
                newsDao.getSimilarStories(trenutnaVijest.uuid).filter { it.uuid != trenutnaVijest.uuid }
            } else if (tagoviZaPrikaz.isNotEmpty()) {
                savedNewsRepository.dohvatiSlicneVijesti(tagoviZaPrikaz.take(2)).filter { it.uuid != trenutnaVijest.uuid }
            } else {
                emptyList()
            }
        }

        ucitavanjeDetaljaUToku = false
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
        if (ucitavanjeDetaljaUToku && odabranaVijest == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ucitavamo detalje vijesti...", modifier = Modifier.padding(top = 8.dp))
            }
        } else if (odabranaVijest != null) {
            Column(
                modifier = Modifier.padding(paddingValues).padding(16.dp).shadow(12.dp, shape = MaterialTheme.shapes.medium).background(plavaKartica, shape = MaterialTheme.shapes.medium).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!odabranaVijest!!.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(odabranaVijest!!.imageUrl).crossfade(true).build(),
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
                    text = odabranaVijest!!.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("details_title").padding(top = 16.dp).padding(bottom = 8.dp)
                )
                Text(
                    text = odabranaVijest!!.snippet,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("details_snippet")
                )
                Spacer(Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Kategorija: ${odabranaVijest!!.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.testTag("details_category").background(Color(90, 90, 160), shape = MaterialTheme.shapes.small).padding(horizontal = 5.dp, vertical = 5.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Izvor: ${odabranaVijest!!.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.testTag("details_source").background(Color(90, 90, 160), shape = MaterialTheme.shapes.small).padding(horizontal = 5.dp, vertical = 5.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Datum objave: ${odabranaVijest!!.publishedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.testTag("details_date").background(Color(90, 90, 160), shape = MaterialTheme.shapes.small).padding(horizontal = 5.dp, vertical = 5.dp)
                    )
                    if (tagoviSlikeIzApi.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tagovi slike (Imagga API):",
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        tagoviSlikeIzApi.forEach { tag ->
                            Text(text = "• $tag", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else if (!odabranaVijest!!.imageUrl.isNullOrBlank()) {
                        Text(text = "", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (tagoviZaPrikaz.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tagovi slike:",
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        tagoviZaPrikaz.forEach { tag ->
                            Text(text = "• $tag", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else if (!odabranaVijest!!.imageUrl.isNullOrBlank()) {
                        Text(text = "Nema dostupnih tagova za ovu vijest.", style = MaterialTheme.typography.bodyMedium)
                    }


                    if (porukaGreske != null) {
                        Text(text = porukaGreske!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
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
                povezaneVijesti(povezaneVijesti = vijestiKojeSuSlicne, navController = navController)
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
                if (porukaGreske != null) {
                    Text(text = porukaGreske!!, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun povezaneVijesti(povezaneVijesti: List<NewsItem>, navController: NavController) {
    BackHandler {
        navController.popBackStack("/home", inclusive = false)
    }
    Column(modifier = Modifier.testTag("related_news_list"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (povezaneVijesti.isNotEmpty()) {
            povezaneVijesti.forEachIndexed { index, vijest ->
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