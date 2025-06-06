package etf.ri.rma.newsfeedapp.screen


import etf.ri.rma.newsfeedapp.data.network.NewsDAO
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
import etf.ri.rma.newsfeedapp.data.network.ImagaDAO

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailsScreen(newsId: String, navController: NavController, newsDao: NewsDAO
) {

    val vijest = remember(newsId) { newsDao.getNewsItem(newsId) }
    val imgDao = remember { ImagaDAO() }
    var isLoadingDetails by remember { mutableStateOf(true) }
    var tagoviZaSliku by remember { mutableStateOf<List<String>>(emptyList()) }
    var vijestiKojeSuSlicne by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var porukaGreske by remember { mutableStateOf<String?>(null) }







    LaunchedEffect(newsId, vijest) {
        porukaGreske = null
         isLoadingDetails = true
        if (vijest == null) {
            porukaGreske = "Vijest s ID-om $newsId nije pronađena."
            isLoadingDetails = false
            return@LaunchedEffect
        }

         var trenutniTagovi: List<String> = emptyList()

        if (vijest.imageUrl != null && vijest.imageUrl.isNotBlank()) {
            try {
                trenutniTagovi = imgDao.getTags(vijest.imageUrl)
                tagoviZaSliku = trenutniTagovi
            } catch (e: InvalidImageURLException) {
                porukaGreske = "Greška pri učitavanju tagova slike: ${e.message}"
             } catch (e: Exception) {
                porukaGreske = "Nepoznata greška pri učitavanju tagova: ${e.message}"
           }
        } else {
          tagoviZaSliku = emptyList()
       }

        var trenutneSlicneVijesti: List<NewsItem> = emptyList()
        try {
            trenutneSlicneVijesti = newsDao.getSimilarStories(newsId)
            vijestiKojeSuSlicne = trenutneSlicneVijesti
        } catch (e: InvalidUUIDException) {
            porukaGreske = "Greška pri učitavanju sličnih vijesti: ${e.message}"
       } catch (e: Exception) {
            porukaGreske = "Nepoznata greška pri učitavanju sličnih vijesti: ${e.message}"
        } finally {
            isLoadingDetails = false
       }
    }
    val pozadinskaBoja = Color(0xFFD3D3D3)
    val plavaKartica = Color(0xFFE6E6FA)

    Scaffold(
        containerColor = pozadinskaBoja, topBar = {
            TopAppBar(
                title = { Text("Detalji vijesti", fontWeight = FontWeight.Bold)
                }, navigationIcon = {
                    IconButton(onClick = {
                        val popped = navController.popBackStack("/home", inclusive = false)
                        if (popped==false) {
                            navController.navigate("/home") {
                                popUpTo("/home") { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Nazad")
                    }
                },
                modifier = Modifier.background(pozadinskaBoja).statusBarsPadding(),
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = pozadinskaBoja
                )
            )
        },
        bottomBar = {
            Button(
                modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("details_close_button"),
                onClick = { navController.popBackStack("/home", inclusive = false) }
            ) {
                Text("Zatvori detalje")
            }
        }
    ) { paddingValues ->
       if (isLoadingDetails && vijest == null) {
           Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
               Text("Učitavam detalje vijesti...", modifier = Modifier.padding(top = 8.dp))
            }
        } else if (vijest != null) {
            Column(
                modifier = Modifier.padding(paddingValues).padding(16.dp).shadow(12.dp, shape = MaterialTheme.shapes.medium).background(plavaKartica, shape = MaterialTheme.shapes.medium).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (vijest.imageUrl.isNullOrBlank()==false) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current).data(vijest.imageUrl).crossfade(true).build(),
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
                    text = vijest.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("details_title").padding(top = 16.dp)
                        .padding(bottom = 8.dp)
                )
                Text(
                    text = vijest.snippet, fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("details_snippet")
                )
                Spacer(Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Kategorija: ${vijest.category}", style = MaterialTheme.typography.bodySmall, color = Color.White, fontSize = 14.sp,
                        modifier = Modifier.testTag("details_category").background(Color(90, 90, 160), shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 5.dp, vertical = 5.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Izvor: ${vijest.source}", style = MaterialTheme.typography.bodySmall,
                        color = Color.White, fontSize = 14.sp, modifier = Modifier.testTag("details_source")
                            .background(Color(90, 90, 160), shape = MaterialTheme.shapes.small).padding(horizontal = 5.dp, vertical = 5.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text( modifier = Modifier
                        .testTag("details_date")
                        .background(Color(90, 90, 160), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 5.dp, vertical = 5.dp), text = "Datum objave: ${vijest.publishedDate}",
                        style = MaterialTheme.typography.bodySmall, color = Color.White,
                        fontSize = 14.sp,
                    )


                    if (tagoviZaSliku.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tagovi slike:",
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        tagoviZaSliku.forEach { tag ->
                            Text(text = "• $tag", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else if (vijest.imageUrl != null && vijest.imageUrl.isNotBlank()) {
                        Text(text = "Nema tagova slike za prikaz.", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (porukaGreske != null) {
                        Text(text = porukaGreske!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Povezane vijesti iz iste kategorije",
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
    Column( modifier = Modifier.testTag("related_news_list"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (povezaneVijesti.isNotEmpty()) {
            povezaneVijesti.forEachIndexed { index, vijest ->
                Text(
                    text = vijest.title,
                    style = MaterialTheme.typography.bodyLarge.copy( fontSize = 15.sp, shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.2f), offset = Offset(2f, 2f))
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { navController.navigate("/details/${vijest.uuid}") }
                        .testTag(
                            if (index ==0 ) "related_news_title_1"
                            else "related_news_title_2")
                )
            }
        } else {
            Text(text = "Nema pronađenih sličnih vijesti u istoj kategoriji.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}