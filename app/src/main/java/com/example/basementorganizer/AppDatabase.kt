package com.example.basementorganizer

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Box::class, Item::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boxDao(): BoxDao
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "basement_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}