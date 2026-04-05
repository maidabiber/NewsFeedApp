package etf.ri.rma.newsfeedapp.data.dao

import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.model.NewsWithTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavedNewsRepository(private val savedNewsDAO: SavedNewsDAO) {


    suspend fun saveNews(news: NewsItem): Boolean = withContext(Dispatchers.IO) {

        savedNewsDAO.saveNews(news)
    }


    suspend fun getAllNews(): List<NewsItem> = withContext(Dispatchers.IO) {

        savedNewsDAO.allNews()
    }



    suspend fun addTagsToNews(tags: List<String>, newsId: Int): Int = withContext(Dispatchers.IO) {

        savedNewsDAO.addTags(tags, newsId)
    }


    suspend fun getSimilarNews(tags: List<String>): List<NewsItem> = withContext(Dispatchers.IO) {

        savedNewsDAO.getSimilarNews(tags)
    }

    suspend fun getNewsIdByUuid(uuid: String): Int? = withContext(Dispatchers.IO) {
        savedNewsDAO.findNewsIdByUuid(uuid)
    }


    suspend fun getNewsWithTag(newsId: Int): NewsWithTags? = withContext(Dispatchers.IO) {
        savedNewsDAO.findNewsWithTags(newsId)
    }


}