package etf.ri.rma.newsfeedapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "News")
data class News(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String,
    val snippet: String,
    val isFeatured: Boolean,
    val imageUrl: String?,
    val category: String,
    val source: String,
    val title: String,
    val publishedDate: String
)
