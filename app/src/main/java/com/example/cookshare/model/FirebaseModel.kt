package com.example.cookshare.model

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class FirebaseModel {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false) // Disable Firebase local persistence as per requirements
            .build()
    }
    // Explicitly initialize with the bucket from google-services.json to avoid "Object does not exist at location"
    // when the default bucket might not be correctly resolved or when using newer Firebase versions.
    private val storage = FirebaseStorage.getInstance("gs://cookshare-1e135.firebasestorage.app")

    fun getCurrentUser() = auth.currentUser
    fun getCurrentUserId() = auth.currentUser?.uid

    fun addUser(user: User, callback: (Boolean) -> Unit) {
        db.collection(User.COLLECTION)
            .document(user.id)
            .set(user.toJson())
            .addOnCompleteListener { callback(it.isSuccessful) }
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos) // Compressed for better upload performance
        val data = baos.toByteArray()

        Log.d("FirebaseModel", "Starting upload to: ${storageRef.path}")

        storageRef.putBytes(data)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("FirebaseModel", "Upload success. URL: ${uri}")
                    callback(uri.toString())
                }.addOnFailureListener {
                    Log.e("FirebaseModel", "Failed to get download URL", it)
                    callback(null)
                }
            }
            .addOnFailureListener {
                Log.e("FirebaseModel", "Upload failed", it)
                callback(null)
            }
    }

    // Recipe operations
    fun getAllRecipes(since: Long, callback: (List<Recipe>) -> Unit) {
        db.collection(Recipe.COLLECTION)
            .whereGreaterThan(Recipe.LAST_UPDATED, Timestamp(since, 0))
            .get()
            .addOnSuccessListener { querySnapshot ->
                val recipes = querySnapshot.documents.mapNotNull { it.data?.let { data -> Recipe.fromJson(data) } }
                callback(recipes)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun addRecipe(recipe: Recipe, callback: (Boolean) -> Unit) {
        db.collection(Recipe.COLLECTION)
            .document(recipe.id)
            .set(recipe.toJson())
            .addOnCompleteListener { callback(it.isSuccessful) }
    }

    fun uploadRecipeImage(recipeId: String, bitmap: Bitmap, callback: (String?) -> Unit) {
        val storageRef = storage.reference.child("recipe_images/$recipeId.jpg")
        uploadBitmap(storageRef, bitmap, callback)
    }
}
