package com.example.cookshare.model

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class FirebaseModel {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
    }
    private val storage = FirebaseStorage.getInstance()

    fun getCurrentUser() = auth.currentUser
    fun getCurrentUserId() = auth.currentUser?.uid

    fun addUser(user: User, callback: (Boolean) -> Unit) {
        db.collection(User.COLLECTION)
            .document(user.id)
            .set(user.toJson())
            .addOnCompleteListener { callback(it.isSuccessful) }
    }

    fun updateUser(user: User, callback: (Boolean) -> Unit) {
        addUser(user, callback)
    }

    fun getUser(userId: String, callback: (User?) -> Unit) {
        db.collection(User.COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    callback(User.fromJson(document.data!!))
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { callback(null) }
    }

    fun uploadImage(userId: String, bitmap: Bitmap, callback: (String?) -> Unit) {
        val storageRef = storage.reference.child("profile_images/$userId.jpg")
        uploadBitmap(storageRef, bitmap, callback)
    }

    private fun uploadBitmap(storageRef: com.google.firebase.storage.StorageReference, bitmap: Bitmap, callback: (String?) -> Unit) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }
            }
            .addOnFailureListener { callback(null) }
    }

    // Recipe operations
    fun getAllRecipes(callback: (List<Recipe>) -> Unit) {
        db.collection("recipes")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val recipes = querySnapshot.documents.mapNotNull { it.data?.let { data -> Recipe.fromJson(data) } }
                callback(recipes)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun addRecipe(recipe: Recipe, callback: (Boolean) -> Unit) {
        db.collection("recipes")
            .document(recipe.id)
            .set(recipe.toJson())
            .addOnCompleteListener { callback(it.isSuccessful) }
    }

    fun uploadRecipeImage(recipeId: String, bitmap: Bitmap, callback: (String?) -> Unit) {
        val storageRef = storage.reference.child("recipe_images/$recipeId.jpg")
        uploadBitmap(storageRef, bitmap, callback)
    }
}
