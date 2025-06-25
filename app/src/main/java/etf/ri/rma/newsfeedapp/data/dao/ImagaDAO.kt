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
        private const val apiKey    = "acc_d64f6eccf9a1409"
        private const val apiSecret = "84d74f3d2b28f0b1674041129c663113"
        private const val baseUrl   = "https://api.imagga.com/"
        private lateinit var apiServis: ImagaApiService
        private val kesTagova   = ConcurrentHashMap<String, ArrayList<String>>()

        private val credentials = Base64.encodeToString("$apiKey:$apiSecret".toByteArray(), Base64.NO_WRAP)
        private val cacheMutex  = Mutex()

    }


    constructor() {
        val httpKlijent = OkHttpClient.Builder().addInterceptor { ch ->
            val zahtjevZaSliku = ch.request().newBuilder().header("Authorization", "Basic $credentials").build()
            ch.proceed(zahtjevZaSliku)
        }.build()

        val instancaRetrofitKlijent = Retrofit.Builder().baseUrl(baseUrl).client(httpKlijent).addConverterFactory(
            GsonConverterFactory.create()).build()

        apiServis = instancaRetrofitKlijent.create(ImagaApiService::class.java)
    }

    fun postaviApiServis(service: ImagaApiService) { apiServis = service }



    suspend fun getTags(imageUrl: String): ArrayList<String> = cacheMutex.withLock {
        kesTagova[imageUrl]?.let {
             return it
        }

        try {
            URL(imageUrl).toURI()
          } catch (izuzetak: Exception) {
             throw InvalidImageURLException("URL slike nije ispravan: $imageUrl")
        }

        val listaTagova = withContext(Dispatchers.IO) {
            try {

                val odgovorApi = apiServis.getTags(imageUrl)

                val dohvaceniTagovi =
                    odgovorApi.result.tags.map { it.tag.en }.toCollection(ArrayList())
                dohvaceniTagovi
            } catch (greska: HttpException) {
                val errorBody = greska.response()?.errorBody()?.string()
                if (greska.code() == 403 || greska.code() == 401) {
                }
                throw InvalidImageURLException("greska pri dohvatanju tagova: ${greska.message}. HTTP kod: ${greska.code()}. Odgovor: $errorBody")
            } catch (e: Exception) {
                throw InvalidImageURLException("greska pri dohvatanju tagova: ${e.message}. Provjerite API URL i ključeve.")
            }
        }
        kesTagova[imageUrl] = listaTagova
        listaTagova
    }
}