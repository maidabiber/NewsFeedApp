package etf.ri.rma.newsfeedapp.screen

import etf.ri.rma.newsfeedapp.data.dao.NewsDAO
import etf.ri.rma.newsfeedapp.data.dao.ImagaDAO
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
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
import androidx.compose.runtime.rememberCoroutineScope
import etf.ri.rma.newsfeedapp.data.dao.SavedNewsRepository
import kotlinx.coroutines.launch

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

   LaunchedEffect(newsId) {
       ucitavanjeDetaljaUToku = true
       jeLiVijestSacuvana = false
        porukaGreske = null


       var idVijestiIzBaze: Int? = null
        try {
            idVijestiIzBaze = savedNewsRepository.dohvatiIdVijestiPoUuid(newsId)
            if (idVijestiIzBaze != null) {
                val vijestiSaTagovima = savedNewsRepository.dohvatiVijestSaTagovima(idVijestiIzBaze)
                vijestiSaTagovima?.let {
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
                       imageTags = it.tags  )
                    tagoviVijestiIzBaze = savedNewsRepository.dohvatiTagoveZaVijest(idVijestiIzBaze)
                    jeLiVijestSacuvana = true
                }
            }
        } catch (e: Exception) {
            porukaGreske = "greska, dohvatanje iz baze nije uspjelo: ${e.message}"
            odabranaVijest = null
        }

       if (odabranaVijest == null) {
            try {
                odabranaVijest = newsDao.getNewsItem(newsId)
                jeLiVijestSacuvana = false
            } catch (e: InvalidUUIDException) {
                porukaGreske = "greska pri dohvatu vijesti s weba: ${e.message}"
            } catch (e: Exception) {
                porukaGreske = "greska pri dohvatu vijesti s weba: ${e.message}"
            }
        }

       odabranaVijest?.let { trenutnaVijest ->
           if (!trenutnaVijest.imageUrl.isNullOrBlank()) {
               try {
                   tagoviSlikeIzApi = imgDao.getTags(trenutnaVijest.imageUrl)
               } catch (e: InvalidImageURLException) {
                   porukaGreske = "greska pri ucitavanju tagova : ${e.message}"
               } catch (e: Exception) {
                   porukaGreske = "greska pri ucitavanju tagova slike: ${e.message}"
               }
           } else {
               tagoviSlikeIzApi = emptyList()
           }


           if (idVijestiIzBaze != null && tagoviVijestiIzBaze.isNotEmpty()) {
               try {
                   val slicnePronadjeneVijesti = savedNewsRepository.dohvatiSlicneVijesti(tagoviVijestiIzBaze)
                   vijestiKojeSuSlicne = slicnePronadjeneVijesti.filter { it.uuid != trenutnaVijest.uuid }
               } catch (e: Exception) {
                   porukaGreske = "greska, nije ispravno pronalazenje slicnih vijesti iz baze: ${e.message}"
                   vijestiKojeSuSlicne = emptyList()
               }
           } else {
               vijestiKojeSuSlicne = emptyList()
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
              if (odabranaVijest != null) {
                    Button(
                        onClick = {
                            scope.launch {
                                if (jeLiVijestSacuvana) {
                                    savedNewsRepository.izbrisiSacuvaneVijesti(odabranaVijest!!.uuid)
                                    jeLiVijestSacuvana = false
                                    tagoviVijestiIzBaze = emptyList()
                                    vijestiKojeSuSlicne = emptyList()
                                } else {
                                    val jeUspjesnoSacuvana = savedNewsRepository.sacuvajVijest(odabranaVijest!!)
                                    if (jeUspjesnoSacuvana) {
                                        jeLiVijestSacuvana = true
                                        if (tagoviSlikeIzApi.isNotEmpty()) {
                                            val sacuvanIdVijesti = savedNewsRepository.dohvatiIdVijestiPoUuid(odabranaVijest!!.uuid)
                                            if (sacuvanIdVijesti != null) {
                                                savedNewsRepository.dodajTagoveZaVijest(tagoviSlikeIzApi, sacuvanIdVijesti)
                                                tagoviVijestiIzBaze = savedNewsRepository.dohvatiTagoveZaVijest(sacuvanIdVijesti)
                                                vijestiKojeSuSlicne = savedNewsRepository.dohvatiSlicneVijesti(tagoviVijestiIzBaze).filter { it.uuid != odabranaVijest!!.uuid }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_delete_button")
                            .padding(bottom = 8.dp)
                    ) {
                        Text(if (jeLiVijestSacuvana) "Ukloni spremljenu vijest" else "Spremi vijest")
                    }
                }
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
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text("Ucitavamo detalje vijesti...", modifier = Modifier.padding(top = 8.dp))
            }
        } else if (odabranaVijest != null) {
            Column(
                modifier = Modifier.padding(paddingValues).padding(16.dp).shadow(12.dp, shape = MaterialTheme.shapes.medium).background(plavaKartica, shape = MaterialTheme.shapes.medium).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!odabranaVijest!!.imageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current).data(odabranaVijest!!.imageUrl).crossfade(true).build(),
                        placeholder = androidx.compose.ui.res.painterResource(etf.ri.rma.newsfeedapp.R.drawable.bosnjaci),
                        error = androidx.compose.ui.res.painterResource(etf.ri.rma.newsfeedapp.R.drawable.bosnjaci),
                        contentDescription = "Naslovna slika vijesti",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                    )
                } else {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(etf.ri.rma.newsfeedapp.R.drawable.bosnjaci),
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
                        Text(text = "Nema tagova slike s Imagga API-ja.", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (tagoviVijestiIzBaze.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tagovi vijesti (iz baze):",
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        tagoviVijestiIzBaze.forEach { tag ->
                            Text(text = "• $tag", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Text(text = "Nema spremljenih tagova za ovu vijest u bazi.", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (porukaGreske != null) {
                        Text(text = porukaGreske!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Povezane vijesti iz baze (po tagovima):",
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
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    "Vijest nije pronađena ili je došlo do greške pri učitavanju.",
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