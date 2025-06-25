package etf.ri.rma.newsfeedapp.data.network

import com.google.gson.annotations.SerializedName
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NewsApiResponse(
    val data: List<NewsApiArticle>
)

data class NewsApiArticle(
    val uuid: String,
    val title: String,
    val snippet: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    val categories: List<String>,
    @SerializedName("is_featured")
    val isFeatured: Boolean,
    val source: String,
    @SerializedName("published_at")
    val publishedAt: String // <--- Ostavite samo publishedAt (String)
    // UKLONITE: val publishedDate: Date, // <-- OVO JE BILO VIŠAK I UZROKOVALO GREŠKU
    // UKLONITE: val similar: List<SimilarNewsApiArticle>? = null // Test ne koristi ovo, ali za aplikaciju ga možete ostaviti
) {
    fun toNewsItem(): NewsItem {
        val kategorija = categories.firstOrNull() ?: "General"
        val formatiranDatum = try {
            val parsiranje = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val formatiranje = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            formatiranje.format(parsiranje.parse(publishedAt))
        } catch (e: Exception) {
            try {
                val parsiranjeFallback = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatiranje = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                formatiranje.format(parsiranjeFallback.parse(publishedAt.split("T").firstOrNull() ?: "1970-01-01"))
            } catch (e2: Exception) {
                "01-01-1970"
            }
        }

        return NewsItem(
            uuid = uuid,
            title = title,
            snippet = snippet,
            imageUrl = imageUrl,
            category = kategorija.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            },
            isFeatured = isFeatured,
            source = source,
            publishedDate = formatiranDatum
        )
    }

}

// Ako vam ne treba u aplikaciji za API pozive, možete je i ukloniti
// Ali za sada je ostavljamo jer ne smeta build-u.
data class SimilarNewsApiArticle(
    val uuid: String,
    val title: String,
    val snippet: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    val categories: List<String>,
    val source: String,
    @SerializedName("published_at")
    val publishedAt: String
)