package etf.ri.rma.newsfeedapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import etf.ri.rma.newsfeedapp.data.dao.SavedNewsDAO
import etf.ri.rma.newsfeedapp.model.News
import etf.ri.rma.newsfeedapp.model.Tags
import etf.ri.rma.newsfeedapp.model.NewsTags

@Database(
    entities = [News::class, Tags::class, NewsTags::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun savedNewsDAO(): SavedNewsDAO

    companion object {
        @Volatile
        private var jedinstvenaInstancaBaze: NewsDatabase? = null

        fun getInstance(kontekst: android.content.Context): NewsDatabase {
            return jedinstvenaInstancaBaze ?: synchronized(this) {
                val novaInstanca = androidx.room.Room.databaseBuilder(
                    kontekst.applicationContext,
                    NewsDatabase::class.java,
                    "news-db"
                )
                     .fallbackToDestructiveMigration()
                     .build()
                jedinstvenaInstancaBaze = novaInstanca
                novaInstanca
            }
        }
    }
}