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
    
    // We use a single MediatorLiveData for the final output
    val orderedRecipes = MediatorLiveData<List<RecipeWithUser>>()
    
    // Track which user IDs we are currently observing to avoid duplicate sources
    private val observedUserIds = mutableSetOf<String>()

    init {
        // Trigger update when the list of recipes changes
        orderedRecipes.addSource(recipes) {
            updateList()
        }
        
        // Trigger update when sort order changes
        orderedRecipes.addSource(_sortOrder) {
            updateList()
        }
    }

    private fun updateList() {
        val currentRecipes = recipes.value ?: return
        val currentOrder = _sortOrder.value ?: "newest"
        
        // Ensure we are observing every user needed for the current recipes
        currentRecipes.forEach { recipe ->
            if (!observedUserIds.contains(recipe.userId)) {
                observedUserIds.add(recipe.userId)
                val userLiveData = model.getUserById(recipe.userId)
                orderedRecipes.addSource(userLiveData) {
                    // When any user data updates, we refresh the whole list
                    updateList()
                }
            }
        }

        // Create the combined list using currently available data in the model/cache
        // Note: getUserById from Room returns a LiveData, but here we just need to 
        // trigger the UI to refresh. Since updateList is called whenever any 
        // source changes, the UI will stay in sync.
        val listWithUsers = currentRecipes.map { recipe ->
            // We fetch the user by ID. Since getUserById is observing, 
            // the value will eventually be populated.
            RecipeWithUser(recipe, model.getUserById(recipe.userId).value)
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
