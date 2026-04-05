package etf.ri.rma.newsfeedapp.data.dao


import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class NewsDAO {

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
        private const val CATEGORY_API_CALL_COOLDOWN = 30_000L
        private val apiKey = properties.getProperty("NEWS_API_KEY")?: ""
        private const val baseUrl = "https://api.thenewsapi.com/v1/"

        private val categoryMapping = mapOf(
            "politics" to "politics",
            "Sve" to null, "Politika" to "politics",
            "Sport" to "sports",

            "Biznis" to "business", "Zdravlje" to "health", "Zabava" to "entertainment",
            "Tehnologija" to "tech", "Nauka" to "science", "Umjetnost" to "general",
            "Hrana" to "food", "Putovanja" to "travel"
        )

        private val kesiraneVijesti = ConcurrentHashMap<String, NewsItem>()
        private lateinit var apiServis: NewsApiService
        private val similarNewsCache = ConcurrentHashMap<String, List<NewsItem>>()
        private val cacheMutex = Mutex()
        private val similarNewsMutex = Mutex()
        private val lastApiCallTimes = ConcurrentHashMap<String, Long>()



        private val mojePocetneVijesti = listOf(
            NewsItem(
                uuid = "a919fb2a-d2be-4b3c-8cf0-907ceb598977",
                title = "Supreme Court revives straight woman's reverse discrimination claim",
                snippet = "WASHINGTON — The Supreme Court on Thursday revived a woman's claim that she was discriminated against at work because she is straight.",
                imageUrl = "https://media-cldnry.s-nbcnews.com/image/upload/t_nbcnews-fp-1200-630,f_auto,q_auto:best/rockcms/2025-04/250422--Marlean-Ames-ch-1005-8452af.jpg",
                category = "general",
                isFeatured = false,
                source = "nbcnews.com",
                publishedDate = "05-06-2025" // Formatiran datum
            ),
            NewsItem(
                uuid = "30ac1a68-7472-41c2-a29c-4e0159128ee0",
                title = "Trump says he is 'very disappointed' in Elon Musk's attacks on the GOP policy bill",
                snippet = "WASHINGTON — President Donald Trump on Thursday sharply criticized Elon Musk after the Tesla CEO criticized the House-passed GOP policy bill as an \"abomination.\"",
                imageUrl = "https://media-cldnry.s-nbcnews.com/image/upload/t_nbcnews-fp-1200-630,f_auto,q_auto:best/rockcms/2025-05/250528-Trump-and-Musk-RS-9f1f20.jpg",
                category = "politics",
                isFeatured = false,
                source = "nbcnews.com",
                publishedDate = "05-06-2025"
            ),
            NewsItem(
                uuid = "0b749ac7-0fa9-45b5-b55b-8f21b596dd58",
                title = "Dakota Johnson Says ‘Madame Web’ “Wasn’t My Fault” & Blames Flop On Decisions Made By People “Who Don’t Have A Creative Bone In Their Body”",
                snippet = "Dakota Johnson says 'Madame Web's failure was due to some",
                imageUrl = "https://deadline.com/wp-content/uploads/2025/06/dakota-johnson-madame-web.jpg?w=1024",
                category = "entertainment",
                isFeatured = false,
                source = "deadline.com",
                publishedDate = "06-06-2025"
            ),
            NewsItem(
                uuid = "e38bd2de-70a8-478b-a5d2-ec3002eaaedf",
                title = "2025 NBA Finals: Haliburton, SGA lead fashionable arrivals to Game 1",
                snippet = "Before the Finals could tip off, stars from both teams looked to set the tone in the pregame tunnel with their style.",
                imageUrl = "https://a3.espncdn.com/combiner/i?img=/photo/2025/0605/r1503054_2_1296x729_16-9.jpg",
                category = "sports",
                // url = "https://www.espn.com/nba/story/_id/45460015/2025-nba-finals-game-1-tunnel-fashion-thunder-pacers",
                isFeatured = false,
                source = "espn.com",
                publishedDate = "06-06-2025"
            ),
            NewsItem(
                uuid = "d4fab343-5872-467a-b9a5-cdc8179f6d39",
                title = "Kierston Russell, twin sister of 5-star Alabama freshman QB, dies",
                snippet = "Kierston Russell, the twin sister of Alabama freshman quarterback Keelon Russell, died Wednesday, according to a statement from Tuscaloosa police officials.",
                imageUrl = "https://a4.espncdn.com/combiner/i?img=/photo/2022/1015/r1075896_1296x729_16-9.jpg",
                category = "sports",
                // url = "https://www.espn.com/college-football/story/_id/45460370/twin-sister-5-star-alabama-freshman-qb-keelon-russell-dies",
                isFeatured = false,
                source = "espn.com",
                publishedDate = "06-06-2025"
            ),
            NewsItem(
                uuid = "69b942ca-35e3-4b1f-8831-aa7f276fa577",
                title = "Shari Redstone Reveals She Is Undergoing Treatment For Thyroid Cancer",
                snippet = "Shari Redstone, who for months has been leading the charge to merge Paramount with Skydance has, at the same time, been fighting another battle with thyroid can...",
                imageUrl = "https://deadline.com/wp-content/uploads/2024/03/shari-redstone.jpg?w=1024",
                category = "entertainment",
                isFeatured = false,
                source = "deadline.com",
                publishedDate = "06-06-2025"
            ),
            NewsItem(
                uuid = "81c8a76c-2b5f-40d7-aa23-8f3f3591621c",
                title = "Inside the big business of boy paper, a booming sub-industry of the K-pop machine",
                snippet = "K-pop photo card trading is a blood sport that's equal parts lottery and enterprise, and there's money to be made for record companies and fans.",
                imageUrl = "https://i.insider.com/684159589b2a601d01b25879?width=1200&format=jpeg",
                category = "business",
                isFeatured = false,
                source = "businessinsider.com",
                publishedDate = "06-06-2025"
            ),
            NewsItem(
                uuid = "8807db5b-10ec-42af-8d34-e94c2688424a",
                title = "We're lifting our price target on Broadcom after its AI business impresses once again",
                snippet = "Expectations were high coming into Thursday's earnings release, so the stock's pullback is hardly a surprise.",
                imageUrl = "https://image.cnbcfm.com/api/v1/image/106906303-16252503512021-07-02t173401z_1031823769_rc2hco92qtbx_rtrmadp_0_usa-broadcom-ftc.jpeg?v=1695376861&w=1920&h=1080",
                category = "business",
                 isFeatured = false,
                source = "cnbc.com",
                publishedDate = "05-06-2025"
            ),
            NewsItem(
                uuid = "9742754a-e2e4-42f7-beab-305377ea43a5",
                snippet = "WASHINGTON — The Supreme Court on Thursday threw out the Mexican government's lawsuit against U.S. firearms manufacturers accusing them of aiding and abetting...",
                imageUrl = "https://media-cldnry.s-nbcnews.com/image/upload/t_nbcnews-fp-1200-630,f_auto,q_auto:best/rockcms/2025-04/250422-smith-wesson-gun-vl-1007a-f17192.jpg",
                category = "politics",
                isFeatured = false,
                title = "Supreme Court rejects Mexico’s lawsuit against U.S. gun makers",

                source = "nbcnews.com",
                publishedDate = "05-06-2025"
            ),
            NewsItem(

                isFeatured = false,
                source = "nypost.com",
                uuid = "c9a23881-12dd-4005-8982-7b6552a2eb50",
                title = "Supreme Court confirms that employers can fire employees for cannabis use",
                snippet = "The Supreme Court confirmed on Thursday that employers in New York State can fire employees for cannabis use as part of their employment at will.",
                imageUrl = "https://nypost.com/wp-content/uploads/sites/2/2025/06/NYP2_105918135.jpg?quality=75&strip=all&w=1024",
                category = "politics",
                publishedDate = "05-06-2025"
            )
        )

        @JvmStatic
        fun ocistiKesiranjeZbogTestova() {
            kesiraneVijesti.clear()
            similarNewsCache.clear()
            lastApiCallTimes.clear()
        }
    }




    constructor() {
        ocistiKesiranjeZbogTestova()
        val retrofitKlijent = Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create()).build()
        apiServis = retrofitKlijent.create(NewsApiService::class.java)
    }

    fun populateWithInitialNews() {
        runBlocking {
            cacheMutex.withLock {
                if (kesiraneVijesti.isEmpty()) {
                    mojePocetneVijesti.forEach { kesiraneVijesti[it.uuid] = it }
                }
            }
        }
    }

    fun setApiService(servis: NewsApiService) { apiServis = servis }

    fun getMappedCategory(lokalnaKat: String): String =
        categoryMapping[lokalnaKat]?.lowercase(Locale.ROOT) ?: "general"

    fun getNewsItemApiCategory(kategorija: String): String =
        categoryMapping[kategorija]?.replaceFirstChar { it.titlecase(Locale.ROOT) } ?: "General"

    suspend fun getTopStoriesByCategory(kategorija: String, forceRefresh: Boolean = false): List<NewsItem> =
        withContext(Dispatchers.IO) {
            val lastCallTime = lastApiCallTimes[kategorija] ?: 0L
            val currentTime = System.currentTimeMillis()
            val apiCategory = getMappedCategory(kategorija)

            if (!forceRefresh && currentTime - lastCallTime < CATEGORY_API_CALL_COOLDOWN) {
                return@withContext kesiraneVijesti.values.filter {
                    it.isFeatured && it.category.equals(
                        apiCategory,
                        true
                    )
                }
            }

            return@withContext try {
                val apiResponse = apiServis.getNews(
                    apiKey = apiKey,
                    categories = getMappedCategory(kategorija),
                    limit = 40
                )
                if (apiResponse.data.isEmpty()) {
                    return@withContext kesiraneVijesti.values.filter {
                        it.isFeatured && it.category.equals(
                            apiCategory,
                            true
                        )
                    }
                }

                val noveIstaknuteVijesti = mutableListOf<NewsItem>()
                cacheMutex.withLock {
                    val newIds = apiResponse.data.map { it.uuid }.toSet()

                    kesiraneVijesti.values.forEach {
                        if (it.isFeatured && it.category.equals(
                                apiCategory,
                                true
                            ) && it.uuid !in newIds
                        ) {
                            kesiraneVijesti[it.uuid] = it.copy(isFeatured = false)
                        }
                    }

                    apiResponse.data.forEach { apiClanak ->
                        val item = apiClanak.toNewsItem().copy(
                            isFeatured = true,
                            category = getNewsItemApiCategory(kategorija)
                        )
                        kesiraneVijesti[item.uuid] = item
                        noveIstaknuteVijesti += item
                    }
                }
                lastApiCallTimes[kategorija] = currentTime
                noveIstaknuteVijesti
            } catch (e: Exception) {
                emptyList()
            }
        }

    fun getAllStories(): List<NewsItem> = kesiraneVijesti.values.toList()
    suspend fun getSimilarStories(uuid: String): List<NewsItem> = withContext(Dispatchers.IO) {
        if (!uuid.matches(Regex("^[a-fA-F0-9\\-]{36}$"))) {
            throw InvalidUUIDException("UUID nije validan: $uuid")
        }

        similarNewsMutex.withLock {
            similarNewsCache[uuid]?.let {
                return@withContext it.take(2)
            }

            return@withContext try {
                val apiResponse = apiServis.getSimilarStories(uuid, apiKey)

                val rezultat = apiResponse.data.map { apiClanak ->
                    apiClanak.toNewsItem().copy(isFeatured = false)
                }.take(2).also { slicneVijesti ->
                    slicneVijesti.forEach {
                        kesiraneVijesti[it.uuid] = it
                    }
                }

                similarNewsCache[uuid] = rezultat
                rezultat
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun getNewsItem(uuid: String): NewsItem? = kesiraneVijesti[uuid]



}