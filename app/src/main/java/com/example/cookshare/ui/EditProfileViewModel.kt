package com.example.cookshare.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cookshare.model.Model
import com.example.cookshare.model.User
import com.google.firebase.auth.FirebaseAuth

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val model = Model.getInstance(application)
    private val auth = FirebaseAuth.getInstance()
    
    val user: LiveData<User?> = model.getUserById(auth.currentUser?.uid ?: "")

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> = _updateResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun updateProfile(name: String, bitmap: Bitmap?) {
        if (name.isBlank()) {
            _errorMessage.value = "Name cannot be empty"
            return
        }

        val currentUser = user.value ?: return
        _loading.value = true
        
        val updatedUser = currentUser.copy(name = name, lastUpdated = System.currentTimeMillis() / 1000)
        
        model.updateUser(updatedUser, bitmap) { success ->
            _loading.value = false
            _updateResult.value = success
            if (!success) {
                _errorMessage.value = "Failed to update profile"
            }
        }
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }
}