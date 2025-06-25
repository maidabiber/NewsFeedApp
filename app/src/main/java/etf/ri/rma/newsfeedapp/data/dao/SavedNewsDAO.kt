
package etf.ri.rma.newsfeedapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import etf.ri.rma.newsfeedapp.model.News
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.model.NewsTags
import etf.ri.rma.newsfeedapp.model.NewsWithTags
import etf.ri.rma.newsfeedapp.model.Tags

import kotlinx.coroutines.flow.Flow

@Dao
interface SavedNewsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tags): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewsEntity(news: News): Long


    @Delete
    suspend fun deleteNewsEntity(news: News)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewsTagCrossRef(crossRef: NewsTags)

    @Transaction
    @Query("SELECT * FROM News WHERE id = :newsId")
    suspend fun findNewsWithTags(newsId: Int): NewsWithTags?

    @Query("SELECT id FROM News WHERE uuid = :uuid LIMIT 1")
    suspend fun findNewsIdByUuid(uuid: String): Int?

    @Query("SELECT * FROM Tags WHERE value = :value LIMIT 1")
    suspend fun findTagByValue(value: String): Tags?

    @Query("SELECT * FROM News WHERE uuid = :uuid LIMIT 1")
    suspend fun findNewsEntityByUuid(uuid: String): News?




    @Query("""
    SELECT N.id FROM News N
    INNER JOIN NewsTags NT ON N.id = NT.newsId 
    INNER JOIN Tags T ON NT.tagId = T.id      
    WHERE T.value IN (:tags)
    GROUP BY N.id
    ORDER BY N.publishedDate DESC
""")
    suspend fun findNewsIdsBySimilarTags(tags: List<String>): List<Int>

    @Query("SELECT id FROM News WHERE uuid = :uuid LIMIT 1")
    fun getNewsIdByUuidFlow(uuid: String): Flow<Int?>

    @Transaction
    @Query("SELECT * FROM News")
    suspend fun getAllNewsWithTags(): List<NewsWithTags>

    @Transaction
    @Query("SELECT * FROM News WHERE category = :category")
    suspend fun getNewsWithTagsByCategory(category: String): List<NewsWithTags>



    @Transaction
    suspend fun saveNews(newsItem: NewsItem): Boolean {
        val VijestiKojePostoje = findNewsEntityByUuid(newsItem.uuid)
        if (VijestiKojePostoje != null) {
            return false
        }
        val vijest = newsItem.toNewsEntity()
        val IdReda = insertNewsEntity(vijest)
        if (IdReda != -1L) {
            newsItem.id = IdReda.toInt()
            return true
        }
        return false
    }

    @Transaction
    suspend fun addTags(tags: List<String>, newsId: Int): Int {
        var brojDodanihTagova = 0

        tags.forEach { vrijednostTaga ->
            var tag = findTagByValue(vrijednostTaga)

            if (tag == null) {
                val tagId = insertTag(Tags(value = vrijednostTaga)).toInt()
                tag = Tags(id = tagId, value = vrijednostTaga)
                brojDodanihTagova =brojDodanihTagova+1
            }

            insertNewsTagCrossRef(NewsTags(newsId = newsId, tagId = tag.id))
        }
        return brojDodanihTagova
    }


    @Transaction
    suspend fun allNews(): List<NewsItem> {
        return getAllNewsWithTags().map { it.toNewsItemForTest() }
    }

    @Transaction
    suspend fun getNewsWithCategory(category: String): List<NewsItem> {
        return getNewsWithTagsByCategory(category).map { it.toNewsItemForTest() }
    }




    @Transaction
    suspend fun getTags(newsId: Int): List<String> {
        return findNewsWithTags(newsId)?.tags?.map { it.value } ?: emptyList()
    }
    @Transaction
    suspend fun getSimilarNews(tags: List<String>): List<NewsItem> {
        val newsIds = findNewsIdsBySimilarTags(tags)

        val newsItems = newsIds.mapNotNull { newsId ->
            findNewsWithTags(newsId)?.toNewsItemForTest()
        }
        return newsItems
    }

    fun NewsWithTags.toNewsItemForTest(): NewsItem {
        return NewsItem(
            id = this.news.id,
            imageUrl = this.news.imageUrl,
            category = this.news.category,
            isFeatured = this.news.isFeatured,
            uuid = this.news.uuid,
            title = this.news.title,
            snippet = this.news.snippet,
            source = this.news.source,
            publishedDate = this.news.publishedDate,
            imageTags = this.tags
        )
    }
    fun NewsItem.toNewsEntity(): News {
        return News(
            category = this.category,
            isFeatured = this.isFeatured,
            source = this.source,
            publishedDate = this.publishedDate,
            uuid = this.uuid,
            title = this.title,
            snippet = this.snippet,
            imageUrl = this.imageUrl
        )
    }




}