package etf.rma.newsfeedapp.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.data.NewsData
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailsScreen(newsId: String, navController: NavController) {
    val vijest = remember(newsId) { NewsData.getNewsById(newsId) }
    val pozadinskaBoja = Color(0xFF, 0xD3, 0xD3, 0xD3)
    val plavaKartica = Color(0xFFE6E6FA)
    Scaffold(
        containerColor= pozadinskaBoja,
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalji vijesti", fontWeight = FontWeight.Bold)
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
            Button(modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("details_close_button"),
                onClick = { navController.popBackStack("/home", inclusive = false)
                }

            ) {
                Text("Zatvori detalje")
            }
        }
    ) { paddingValues ->
        if (vijest != null) {
            Column(
                modifier = Modifier.padding(paddingValues).padding(16.dp).shadow(12.dp, shape = MaterialTheme.shapes.medium).background(plavaKartica,  shape = MaterialTheme.shapes.medium)
                    .padding(16.dp).verticalScroll(rememberScrollState())      ,
                verticalArrangement = Arrangement.spacedBy(8.dp),

            ) {
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
                            .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small).padding(horizontal = 5.dp, vertical = 5.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text( modifier = Modifier
                        .testTag("details_date")
                        .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 5.dp, vertical = 5.dp), text = "Datum objave: ${vijest.publishedDate}",
                        style = MaterialTheme.typography.bodySmall, color = Color.White,
                        fontSize = 14.sp,

                    )
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
                RelatedNewsSection(trenutnaVijest = vijest, navController = navController)
            }
        }
    }
}

@Composable
fun RelatedNewsSection(trenutnaVijest: NewsItem, navController: NavController) {
    val sveVijesti = remember { NewsData.getAllNews() }
    val formatDatuma = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val vijestiKojeSuPovezane = remember(trenutnaVijest) {
        sveVijesti.filter { it.id != trenutnaVijest.id && it.category.equals(trenutnaVijest.category,
                    ignoreCase = true
                )
            }.sortedWith(compareBy(
                {
                    val prviDatum = formatDatuma.parse(it.publishedDate)?.time ?: 0
                    val drugiDatum = formatDatuma.parse(trenutnaVijest.publishedDate)?.time ?: 0
                    Math.abs(prviDatum - drugiDatum) },
                { it.title }
            ))
            .take(2)
    }

    BackHandler {
        navController.popBackStack("/home", inclusive = false)
    }
    Column( modifier = Modifier.testTag("news_list"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        vijestiKojeSuPovezane.forEachIndexed { index, vijesti ->
            Text(
                text = vijesti.title,
                style = MaterialTheme.typography.bodyLarge.copy( fontSize = 15.sp, shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.2f), offset = Offset(2f, 2f))
                ),
                color = MaterialTheme.colorScheme.primary,

                modifier = Modifier
                    .clickable { navController.navigate("/details/${vijesti.id}") }
                    .testTag(
                        if (index ==0 ) "related_news_title_1"
                        else "related_news_title_2")
            )

        }

    }
}