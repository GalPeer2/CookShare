package com.example.cookshare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.cookshare.model.Model
import com.example.cookshare.model.Recipe
import com.example.cookshare.model.User

data class RecipeWithUser(val recipe: Recipe, val user: User?)

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val model = Model.getInstance(application)

    fun getRecipesWithUsers(): LiveData<List<RecipeWithUser>> {
        val recipesLiveData = model.getAllRecipes()
        return MediatorLiveData<List<RecipeWithUser>>().apply {
            addSource(recipesLiveData) {
                recipes ->
                val recipeWithUsers = mutableListOf<RecipeWithUser>()
                recipes.forEach { recipe ->
                    val userLiveData = model.getUserById(recipe.userId)
                    addSource(userLiveData) { user ->
                        val existing = recipeWithUsers.find { it.recipe.id == recipe.id }
                        if (existing != null) {
                            recipeWithUsers.remove(existing)
                        }
                        recipeWithUsers.add(RecipeWithUser(recipe, user))
                        value = recipeWithUsers
                    }
                }
            }
        }
    }
}