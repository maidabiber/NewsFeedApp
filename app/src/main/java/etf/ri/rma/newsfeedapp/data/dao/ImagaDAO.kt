package etf.ri.rma.newsfeedapp.data.dao

import android.util.Base64
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
class ImagaDAO {

    companion object {

        private val properties = java.util.Properties().apply {
            try {

                val file = java.io.File("local.properties")
                if (file.exists()) {
                    java.io.FileInputStream(file).use { load(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private val apiKey    = properties.getProperty("IMAGGA_KEY") ?: ""
        private val apiSecret = properties.getProperty("IMAGGA_SECRET") ?: ""
        private const val baseUrl   = "https://api.imagga.com/"
        private lateinit var apiService: ImagaApiService
        private val tagsCache   = ConcurrentHashMap<String, ArrayList<String>>()

        private val credentials = Base64.encodeToString("$apiKey:$apiSecret".toByteArray(), Base64.NO_WRAP)
        private val cacheMutex  = Mutex()

    }


    constructor() {
        val httpClient = OkHttpClient.Builder().addInterceptor { ch ->
            val imageRequest = ch.request().newBuilder().header("Authorization", "Basic $credentials").build()
            ch.proceed(imageRequest)
        }.build()

        val retrofitInstance = Retrofit.Builder().baseUrl(baseUrl).client(httpClient).addConverterFactory(
            GsonConverterFactory.create()).build()

        apiService = retrofitInstance.create(ImagaApiService::class.java)
    }

    fun setApiService(service: ImagaApiService) { apiService = service }



    suspend fun getTags(imageUrl: String): ArrayList<String> = cacheMutex.withLock {
        tagsCache[imageUrl]?.let {
             return it
        }

        try {
            URL(imageUrl).toURI()
          } catch (izuzetak: Exception) {
             throw InvalidImageURLException("URL slike nije ispravan: $imageUrl")
        }

        val tagList = withContext(Dispatchers.IO) {
            try {

                val apiResponse = apiService.getTags(imageUrl)

                val fetchedTags =
                    apiResponse.result.tags.map { it.tag.en }.toCollection(ArrayList())
                fetchedTags
            } catch (greska: HttpException) {
                val errorBody = greska.response()?.errorBody()?.string()
                if (greska.code() == 403 || greska.code() == 401) {
                }
                throw InvalidImageURLException("Greska pri dohvatanju tagova: ${greska.message}. HTTP kod: ${greska.code()}. Odgovor: $errorBody")
            } catch (e: Exception) {
                throw InvalidImageURLException("Greska pri dohvatanju tagova: ${e.message}. Provjerite API URL i kljuceve.")
            }
        }
        tagsCache[imageUrl] = tagList
        tagList
    }
}