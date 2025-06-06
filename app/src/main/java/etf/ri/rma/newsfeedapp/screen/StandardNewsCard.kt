package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import etf.ri.rma.newsfeedapp.R
import etf.ri.rma.newsfeedapp.model.NewsItem



@Composable
fun StandardNewsCard(nasaTrenutnaVijest: NewsItem, onClick: (String) -> Unit) {
    val pozadinskaBoja = Color(0xFFE6E6FA)
    Card( Modifier.fillMaxWidth().padding( 8.dp).clickable { onClick(nasaTrenutnaVijest.uuid) }, // Koristimo uuid
        shape = RoundedCornerShape(9.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(
            containerColor = pozadinskaBoja
        )) {
        Row( Modifier.padding(13.dp),
            verticalAlignment = Alignment.Top ) {
            Image(
                painter = rememberAsyncImagePainter(nasaTrenutnaVijest.imageUrl),
                contentDescription = nasaTrenutnaVijest.title,
                modifier = Modifier.size(91.dp).clip(RoundedCornerShape(9.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer( Modifier.width(12.dp))
            Column(Modifier.weight(1f),
                verticalArrangement = Arrangement.Top) {
                Text(
                    nasaTrenutnaVijest.title, overflow = TextOverflow.Ellipsis,maxLines = 2,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.SansSerif	,
                        fontWeight = FontWeight.Bold,
                        fontSize=19.5.sp
                    ),
                    modifier = Modifier.padding(top = 0.dp)
                )
                Spacer(Modifier.height(3.dp))
                Text( nasaTrenutnaVijest.snippet,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.5.sp,
                    ), maxLines = 2,
                )
                Spacer( Modifier.height(4.dp))
                Text( "${nasaTrenutnaVijest.source} • ${nasaTrenutnaVijest.publishedDate}",
                    style= MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.5.sp,
                    ) )
            }
        }
    }
}