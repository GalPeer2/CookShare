package com.example.cookshare.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.cookshare.model.Model
import com.example.cookshare.model.Recipe

class MyRecipesViewModel(application: Application) : AndroidViewModel(application) {
    private val model = Model.getInstance(application)

    fun getRecipesForCurrentUser(): LiveData<List<Recipe>> {
        val currentUserId = model.getCurrentUserId()
        if (currentUserId == null) {
            Log.e("LocalStore", "No current user ID found")
            return MutableLiveData(emptyList())
        }
        return model.getRecipesByUserId(currentUserId)
    }
}