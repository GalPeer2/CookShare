package com.example.cookshare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.cookshare.model.Model
import com.example.cookshare.model.Recipe
import com.example.cookshare.model.User

data class RecipeWithUser(val recipe: Recipe, val user: User?)

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val model = Model.getInstance(application)
    private val recipes = model.getAllRecipes()
    
    private val _sortOrder = MutableLiveData("newest")
    
    private val _recipeWithUsersList = MediatorLiveData<List<RecipeWithUser>>()
    val orderedRecipes = MediatorLiveData<List<RecipeWithUser>>()

    init {
        _recipeWithUsersList.addSource(recipes) { recipesList ->
            combine(recipesList, _sortOrder.value ?: "newest")
        }
        
        orderedRecipes.addSource(_recipeWithUsersList) { list ->
            orderedRecipes.value = sortList(list, _sortOrder.value ?: "newest")
        }
        
        orderedRecipes.addSource(_sortOrder) { order ->
            orderedRecipes.value = sortList(_recipeWithUsersList.value ?: emptyList(), order)
        }
    }

    private fun combine(recipesList: List<Recipe>, order: String) {
        val currentList = mutableListOf<RecipeWithUser>()
        recipesList.forEach { recipe ->
            val userLiveData = model.getUserById(recipe.userId)
            _recipeWithUsersList.addSource(userLiveData) { user ->
                val index = currentList.indexOfFirst { it.recipe.id == recipe.id }
                if (index != -1) {
                    currentList[index] = RecipeWithUser(recipe, user)
                } else {
                    currentList.add(RecipeWithUser(recipe, user))
                }
                _recipeWithUsersList.value = currentList
            }
        }
    }

    private fun sortList(list: List<RecipeWithUser>, order: String): List<RecipeWithUser> {
        return when (order) {
            "newest" -> list.sortedByDescending { it.recipe.createdAt }
            "popular" -> list.sortedByDescending { it.recipe.likedBy.size }
            else -> list
        }
    }

    fun setOrder(order: String) {
        _sortOrder.value = order
    }
}