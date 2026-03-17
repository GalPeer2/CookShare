package com.example.cookshare.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cookshare.data.models.Recipe

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe")
    fun getAll(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipe WHERE id = :recipeId")
    fun getRecipeById(recipeId: String): LiveData<Recipe?>

    @Query("SELECT * FROM recipe WHERE userId = :userId")
    fun getRecipesByUserId(userId: String): LiveData<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(vararg recipes: Recipe)

    @Delete
    fun delete(recipe: Recipe)

    @Query("DELETE FROM recipe")
    fun deleteAll()

    @Query("SELECT MAX(lastUpdated) FROM recipe")
    fun getMaxLastUpdated(): Long
}