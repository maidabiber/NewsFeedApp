package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import etf.ri.rma.newsfeedapp.R
import etf.ri.rma.newsfeedapp.model.NewsItem

@Composable
fun FeaturedNewsCard(nasaTrenutnaVijest: NewsItem) {

    Card ( Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(9.dp),
        elevation = CardDefaults.cardElevation(5.dp)
    ) { Column(Modifier.padding(9.dp)) {
            Image(
                painterResource(R.drawable.bosnjaci),
                "image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))

        Spacer(modifier = Modifier.height(8.dp)) //treba mi ovaj razmak ipak
            Text(nasaTrenutnaVijest.title, maxLines = 2,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize= 19.5.sp
                ),
              // ovo je za preklapanje
                overflow = TextOverflow.Ellipsis )

            Spacer(Modifier.height(3.dp))

            Text(maxLines = 2,
                text = nasaTrenutnaVijest.snippet,
                //color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.5.sp,),
                overflow = TextOverflow.Ellipsis )
            Spacer(Modifier.height(4.dp))


            Text(text = "${nasaTrenutnaVijest.source} • ${nasaTrenutnaVijest.publishedDate}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.5.sp,)
            )
        }
    }
}
