package com.example.cookshare.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cookshare.model.Model
import com.example.cookshare.model.User
import com.google.firebase.auth.FirebaseAuth

class SignupViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val model = Model.getInstance(application)

    private val _signupResult = MutableLiveData<Boolean>()
    val signupResult: LiveData<Boolean> = _signupResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun signup(email: String, password: String, name: String, bitmap: Bitmap?) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _errorMessage.value = "Please fill all fields"
            return
        }

        _loading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: ""
                    val user = User(id = userId, email = email, name = name)
                    model.signup(user, bitmap) { success ->
                        _loading.value = false
                        _signupResult.value = success
                        if (!success) {
                            _errorMessage.value = "Failed to save user data"
                        }
                    }
                } else {
                    _loading.value = false
                    _errorMessage.value = task.exception?.message ?: "Signup failed"
                    _signupResult.value = false
                }
            }
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }
}