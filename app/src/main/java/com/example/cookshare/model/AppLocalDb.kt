package com.example.cookshare.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [User::class, Recipe::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun recipeDao(): RecipeDao
}

object AppLocalDb {
    @Volatile
    private var instance: AppLocalDbRepository? = null

    fun getDatabase(context: Context): AppLocalDbRepository {
        return instance ?: synchronized(this) {
            val res = Room.databaseBuilder(
                context.applicationContext,
                AppLocalDbRepository::class.java,
                "dbfile.db"
            ).fallbackToDestructiveMigration().build()
            instance = res
            res
        }
    }
}