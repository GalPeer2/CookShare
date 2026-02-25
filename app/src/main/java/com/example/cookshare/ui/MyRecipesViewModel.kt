package com.example.cookshare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.cookshare.model.Model
import com.example.cookshare.model.Recipe

class MyRecipesViewModel(application: Application) : AndroidViewModel(application) {
    private val model = Model.getInstance(application)
    private val allRecipes = model.getAllRecipes()

    fun getRecipesForCurrentUser(): LiveData<List<Recipe>> {
        val currentUserId = model.getCurrentUserId()
        return allRecipes.map { recipes ->
            recipes.filter { it.userId == currentUserId }
        }
    }
}