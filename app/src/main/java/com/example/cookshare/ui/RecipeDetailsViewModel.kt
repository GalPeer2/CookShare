package com.example.cookshare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.cookshare.model.Model
import com.example.cookshare.model.Recipe
import com.example.cookshare.model.User
import kotlinx.coroutines.launch

class RecipeDetailsViewModel(application: Application, recipeId: String) : AndroidViewModel(application) {

    private val model = Model.getInstance(application)

    val recipe: LiveData<Recipe?> = model.getRecipeById(recipeId)
    
    val user: LiveData<User?> = recipe.switchMap { recipe ->
        if (recipe != null) {
            model.getUserById(recipe.userId)
        } else {
            MutableLiveData(null)
        }
    }

    fun toggleLike(recipe: Recipe) {
        viewModelScope.launch {
            val currentUser = model.getCurrentUser()
            currentUser?.let {
                val newLikedBy = if (recipe.likedBy.contains(it.uid)) {
                    recipe.likedBy - it.uid
                } else {
                    recipe.likedBy + it.uid
                }
                model.addRecipe(recipe.copy(likedBy = newLikedBy)) {}
            }
        }
    }
}