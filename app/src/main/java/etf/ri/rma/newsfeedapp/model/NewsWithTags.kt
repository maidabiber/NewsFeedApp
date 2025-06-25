package etf.ri.rma.newsfeedapp.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NewsWithTags(
    @Embedded val news: News,
    @Relation(
        entityColumn = "id", parentColumn = "id", associateBy = Junction(
            value = NewsTags::class,
            entityColumn = "tagId",
            parentColumn = "newsId"

        )
    )
    val tags: List<Tags>
)
