package com.ziaafridi.notifyvault

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RecoveredMessage::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns with default values
                database.execSQL("ALTER TABLE recovered_messages ADD COLUMN conversationId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE recovered_messages ADD COLUMN conversationType TEXT NOT NULL DEFAULT 'INDIVIDUAL'")
                database.execSQL("ALTER TABLE recovered_messages ADD COLUMN notificationKey TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE recovered_messages ADD COLUMN messageHash TEXT NOT NULL DEFAULT ''")
                
                // Update existing records with proper values
                database.execSQL("UPDATE recovered_messages SET conversationId = sender WHERE conversationId = ''")
                database.execSQL("UPDATE recovered_messages SET notificationKey = id || '_' || timestamp WHERE notificationKey = ''")
                database.execSQL("UPDATE recovered_messages SET messageHash = substr(message || sender || timestamp, 1, 32) WHERE messageHash = ''")
                
                // Create indices
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recovered_messages_notificationKey ON recovered_messages(notificationKey)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recovered_messages_conversationId_timestamp ON recovered_messages(conversationId, timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recovered_messages_sender_timestamp ON recovered_messages(sender, timestamp)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add media-related columns
                database.execSQL("ALTER TABLE recovered_messages ADD COLUMN mediaType TEXT")
                database.execSQL("ALTER TABLE recovered_messages ADD COLUMN mediaFileName TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add avatar/profile image path
                database.execSQL("ALTER TABLE recovered_messages ADD COLUMN profileImagePath TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notify_vault_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}