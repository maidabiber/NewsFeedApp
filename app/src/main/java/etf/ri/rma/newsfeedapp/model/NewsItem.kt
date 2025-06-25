package etf.ri.rma.newsfeedapp.model


data class NewsItem(
    var id: Int = 0,
    val isFeatured: Boolean,
    val source: String,
    val imageUrl: String?,
    val category: String,
    val publishedDate: String,
    val uuid: String,
    val title: String,
    val snippet: String,
    val imageTags: List<Tags> = emptyList()
){

}