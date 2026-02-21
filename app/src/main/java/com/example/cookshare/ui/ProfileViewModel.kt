package com.example.cookshare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.cookshare.model.Model
import com.example.cookshare.model.User
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val model = Model.getInstance(application)
    private val auth = FirebaseAuth.getInstance()
    
    val user: LiveData<User?> = model.getUserById(auth.currentUser?.uid ?: "")
    val loadingState = model.userLoadingState

    fun logout() {
        auth.signOut()
    }
}