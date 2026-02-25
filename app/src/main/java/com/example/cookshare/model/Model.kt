package com.example.cookshare.model

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.Executors

class Model private constructor(context: Context) {

    private val database = AppLocalDb.getDatabase(context)
    private val firebaseModel = FirebaseModel()
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        @Volatile
        private var _instance: Model? = null

        fun getInstance(context: Context): Model {
            return _instance ?: synchronized(this) {
                _instance ?: Model(context.applicationContext).also { _instance = it }
            }
        }

        val instance: Model
            get() = _instance ?: throw IllegalStateException("Model not initialized")
    }

    enum class LoadingState {
        LOADING,
        LOADED,
        ERROR
    }

    val userLoadingState = MutableLiveData<LoadingState>()
    val recipesLoadingState = MutableLiveData<LoadingState>()

    fun getCurrentUser() = firebaseModel.getCurrentUser()
    fun getCurrentUserId() = firebaseModel.getCurrentUserId()

    fun getUserById(id: String): LiveData<User?> {
        refreshUser(id)
        return database.userDao().getUserById(id)
    }

    private fun refreshUser(id: String) {
        userLoadingState.postValue(LoadingState.LOADING)
        firebaseModel.getUser(id) { user ->
            if (user != null) {
                executor.execute {
                    database.userDao().insert(user)
                    userLoadingState.postValue(LoadingState.LOADED)
                }
            } else {
                userLoadingState.postValue(LoadingState.ERROR)
            }
        }
    }

    fun signup(user: User, bitmap: Bitmap?, callback: (Boolean) -> Unit) {
        if (bitmap != null) {
            firebaseModel.uploadImage(user.id, bitmap) { url ->
                if (url != null) {
                    val userWithImage = user.copy(profileImageUrl = url)
                    firebaseModel.addUser(userWithImage) { success ->
                        if (success) {
                            executor.execute {
                                database.userDao().insert(userWithImage)
                                mainHandler.post { callback(true) }
                            }
                        } else {
                            callback(false)
                        }
                    }
                } else {
                    callback(false)
                }
            }
        } else {
            firebaseModel.addUser(user) { success ->
                if (success) {
                    executor.execute {
                        database.userDao().insert(user)
                        mainHandler.post { callback(true) }
                    }
                } else {
                    callback(false)
                }
            }
        }
    }

    fun updateUser(user: User, bitmap: Bitmap?, callback: (Boolean) -> Unit) {
        signup(user, bitmap, callback) // Same logic for update in this case
    }

    // Recipe operations
    fun getAllRecipes(): LiveData<List<Recipe>> {
        refreshAllRecipes()
        return database.recipeDao().getAll()
    }

    fun getRecipeById(id: String): LiveData<Recipe?> {
        return database.recipeDao().getRecipeById(id)
    }

    fun refreshAllRecipes() {
        recipesLoadingState.postValue(LoadingState.LOADING)
        firebaseModel.getAllRecipes { recipes ->
            executor.execute {
                database.recipeDao().insert(*recipes.toTypedArray())
                recipesLoadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun addRecipe(recipe: Recipe, callback: (Boolean) -> Unit) {
        firebaseModel.addRecipe(recipe) { success ->
            if (success) {
                executor.execute {
                    database.recipeDao().insert(recipe)
                    mainHandler.post { callback(true) }
                }
            } else {
                mainHandler.post { callback(false) }
            }
        }
    }

    fun uploadRecipeImage(recipeId: String, bitmap: Bitmap, callback: (String?) -> Unit) {
        firebaseModel.uploadRecipeImage(recipeId, bitmap, callback)
    }
}
