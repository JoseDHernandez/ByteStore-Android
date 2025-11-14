package com.example.bytestore.core

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bytestore.data.local.CartDao
import com.example.bytestore.data.local.CartEntity

@Database(
    entities = [CartEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}

object DBProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bytestore_database"
            )
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }

    fun getCartDao(context: Context): CartDao {
        return get(context).cartDao()
    }
}