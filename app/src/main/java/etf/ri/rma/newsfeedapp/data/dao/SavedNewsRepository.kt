package etf.ri.rma.newsfeedapp.data.dao

import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.model.NewsWithTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavedNewsRepository(private val savedNewsDAO: SavedNewsDAO) {


    suspend fun sacuvajVijest(news: NewsItem): Boolean = withContext(Dispatchers.IO) {

        savedNewsDAO.saveNews(news)
    }


    suspend fun sveVijesti(): List<NewsItem> = withContext(Dispatchers.IO) {

        savedNewsDAO.allNews()
    }



    suspend fun dodajTagoveZaVijest(tags: List<String>, newsId: Int): Int = withContext(Dispatchers.IO) {

        savedNewsDAO.addTags(tags, newsId)
    }


    suspend fun dohvatiTagoveZaVijest(newsId: Int): List<String> = withContext(Dispatchers.IO) {

        savedNewsDAO.getTags(newsId)
    }


    suspend fun dohvatiSlicneVijesti(tags: List<String>): List<NewsItem> = withContext(Dispatchers.IO) {

        savedNewsDAO.getSimilarNews(tags)
    }

    suspend fun dohvatiIdVijestiPoUuid(uuid: String): Int? = withContext(Dispatchers.IO) {
        savedNewsDAO.findNewsIdByUuid(uuid)
    }

    suspend fun izbrisiSacuvaneVijesti(uuid: String) = withContext(Dispatchers.IO) {

        val idVijesti = savedNewsDAO.findNewsIdByUuid(uuid)
        if (idVijesti != null) {
            val vijestSaTagovima = savedNewsDAO.findNewsWithTags(idVijesti)
            val vijestZaBrisanje = vijestSaTagovima?.news
            if (vijestZaBrisanje != null) {
                savedNewsDAO.deleteNewsEntity(vijestZaBrisanje)
            }
        }
    }



    suspend fun dohvatiVijestSaTagovima(newsId: Int): NewsWithTags? = withContext(Dispatchers.IO) {
        savedNewsDAO.findNewsWithTags(newsId)
    }


}