package com.example.cookshare.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecipeDetailsViewModelFactory(private val application: Application, private val recipeId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailsViewModel(application, recipeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}