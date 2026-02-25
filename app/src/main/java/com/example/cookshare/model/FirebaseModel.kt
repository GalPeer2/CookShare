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
}