package etf.ri.rma.newsfeedapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import etf.ri.rma.newsfeedapp.data.dao.SavedNewsDAO
import etf.ri.rma.newsfeedapp.model.News
import etf.ri.rma.newsfeedapp.model.Tags
import etf.ri.rma.newsfeedapp.model.NewsTags

@Database(
    entities = [News::class, Tags::class, NewsTags::class],
    version = 2,
    exportSchema = false
)

abstract class NewsDatabase : RoomDatabase() {
    abstract fun savedNewsDAO(): SavedNewsDAO

    companion object {
        @Volatile
        private var instance: NewsDatabase? = null

        fun getInstance(context: android.content.Context): NewsDatabase {
            return instance ?: synchronized(this) {
                val newInstance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java,
                    "news-db"
                )
                     .fallbackToDestructiveMigration()
                     .build()
                instance = newInstance
                newInstance
            }
        }
    }
}