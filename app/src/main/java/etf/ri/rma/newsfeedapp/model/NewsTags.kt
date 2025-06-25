package etf.ri.rma.newsfeedapp.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "NewsTags",
    primaryKeys = ["newsId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = News::class, parentColumns = ["id"], childColumns = ["newsId"]),
        ForeignKey(entity = Tags::class, parentColumns = ["id"], childColumns = ["tagId"])
    ],
            indices = [
        Index(value = ["newsId"]),
        Index(value = ["tagId"])
    ]
)
data class NewsTags(
    val tagId: Int,
    val newsId: Int
)