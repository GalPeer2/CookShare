package com.example.cookshare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.cookshare.model.Model
import com.example.cookshare.data.models.Recipe
import com.example.cookshare.data.models.User

data class RecipeWithUser(val recipe: Recipe, val user: User?)

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val model = Model.getInstance(application)
    private val recipes = model.getAllRecipes()
    
    private val _sortOrder = MutableLiveData("newest")
    
    val orderedRecipes = MediatorLiveData<List<RecipeWithUser>>()
    
    // Cache LiveData instances to avoid redundant database calls and null values
    private val userLiveDatas = mutableMapOf<String, LiveData<User?>>()

    init {
        orderedRecipes.addSource(recipes) {
            updateList()
        }
        
        orderedRecipes.addSource(_sortOrder) {
            updateList()
        }
    }

    private fun updateList() {
        val currentRecipes = recipes.value ?: return
        val currentOrder = _sortOrder.value ?: "newest"
        
        currentRecipes.forEach { recipe ->
            if (!userLiveDatas.containsKey(recipe.userId)) {
                val userLiveData = model.getUserById(recipe.userId)
                userLiveDatas[recipe.userId] = userLiveData
                orderedRecipes.addSource(userLiveData) {
                    updateList()
                }
            }
        }

        val listWithUsers = currentRecipes.map { recipe ->
            // Use the cached LiveData instance
            val user = userLiveDatas[recipe.userId]?.value
            RecipeWithUser(recipe, user)
        }

        orderedRecipes.value = sortList(listWithUsers, currentOrder)
    }

    private fun sortList(list: List<RecipeWithUser>, order: String): List<RecipeWithUser> {
        return when (order) {
            "newest" -> list.sortedByDescending { it.recipe.createdAt }
            "popular" -> list.sortedByDescending { it.recipe.likedBy.size }
            else -> list
        }
    }

    fun setOrder(order: String) {
        if (_sortOrder.value != order) {
            _sortOrder.value = order
        }
    }
}
