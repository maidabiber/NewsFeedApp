package etf.ri.rma.newsfeedapp.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class NewsItem(
    val uuid: String, // uuid mi treba biti jedinstven
    val title: String,
    val snippet: String,
    val imageUrl: String?,
    val category: String,
    val isFeatured: Boolean = false,
    val source: String,
    val publishedDate: String,
    val imageTags: ArrayList<String> = arrayListOf() // Dodan atribut za tagove
){
    fun getPublishedLocalDate(): LocalDate? {
        return try {
            LocalDate.parse(publishedDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } catch (e: Exception) {
            null
        }
    }
}