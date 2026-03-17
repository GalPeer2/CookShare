package com.example.cookshare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.cookshare.model.Model
import com.example.cookshare.data.models.Recipe
import com.example.cookshare.data.models.User
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val model = Model.getInstance(application)
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    
    val user: LiveData<User?> = model.getUserById(userId)
    val loadingState = model.userLoadingState

    private val userRecipes: LiveData<List<Recipe>> = model.getRecipesByUserId(userId)
    
    val recipeCount = MediatorLiveData<Int>()
    val totalLikes = MediatorLiveData<Int>()

    init {
        recipeCount.addSource(userRecipes) { recipes ->
            recipeCount.value = recipes.size
            totalLikes.value = recipes.sumOf { it.likedBy.size }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
