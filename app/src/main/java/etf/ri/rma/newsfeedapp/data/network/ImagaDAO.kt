
package etf.ri.rma.newsfeedapp.data.network
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import android.util.Base64
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ImagaDAO {

    companion object {
        private const val apiKey    = "acc_3c20a31edce0b19"
        private const val apiSecret = "33ed9bb7d0f70cd9db8a38d9facbbcf0"
        private const val baseUrl   = "https://api.imagga.com/"
        private lateinit var apiServis: ImagaApiService
        private val kesTagova   = ConcurrentHashMap<String, ArrayList<String>>()

        private val credentials = Base64.encodeToString("$apiKey:$apiSecret".toByteArray(), Base64.NO_WRAP)
        private val cacheMutex  = Mutex()

    }


    constructor() {
        val klijent = OkHttpClient.Builder().addInterceptor { ch ->
            val zahtjev = ch.request().newBuilder().header("Authorization", "Basic $credentials").build()
            ch.proceed(zahtjev)
        }.build()

        val retr = Retrofit.Builder().baseUrl(baseUrl).client(klijent).addConverterFactory(GsonConverterFactory.create()).build()

        apiServis = retr.create(ImagaApiService::class.java)
    }

    fun postaviApiServis(service: ImagaApiService) { apiServis = service }



    suspend fun getTags(imageUrl: String): ArrayList<String> = cacheMutex.withLock {
        kesTagova[imageUrl]?.let {
             return it
        }

        try {
            URL(imageUrl).toURI()
          } catch (izuzetak: Exception) {
             throw InvalidImageURLException("Neispravan URL slike: $imageUrl")
        }

        val tagovi = withContext(Dispatchers.IO) {
            try {

                val odgovor = apiServis.getTags(imageUrl)

                val dohvaceniTagovi = odgovor.result.tags.map { it.tag.en }.toCollection(ArrayList())
                dohvaceniTagovi
            } catch (greska: retrofit2.HttpException) {
                val errorBody = greska.response()?.errorBody()?.string()
               if (greska.code() == 403 || greska.code() == 401) {
                }
                throw InvalidImageURLException("Greška pri dohvatanju tagova: ${greska.message}. HTTP kod: ${greska.code()}. Odgovor: $errorBody")
            } catch (e: Exception) {
                throw InvalidImageURLException("Greška pri dohvatanju tagova: ${e.message}. Provjerite API URL i ključeve.")
            }
        }
        kesTagova[imageUrl] = tagovi
        tagovi
    }
}
