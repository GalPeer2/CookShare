package com.example.cookshare.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cookshare.model.Converters
import com.example.cookshare.data.models.Recipe
import com.example.cookshare.data.models.User

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