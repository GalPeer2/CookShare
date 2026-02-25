package com.example.cookshare.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe")
    fun getAll(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipe WHERE id = :recipeId")
    fun getRecipeById(recipeId: String): LiveData<Recipe?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg recipes: Recipe)

    @Delete
    fun delete(recipe: Recipe)

    @Query("SELECT MAX(lastUpdated) FROM recipe")
    fun getMaxLastUpdated(): Long
}