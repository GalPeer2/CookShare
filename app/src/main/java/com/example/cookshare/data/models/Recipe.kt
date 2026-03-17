package com.example.cookshare.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

@Entity
data class Recipe(
    @PrimaryKey
    var id: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var userId: String = "",
    var name: String = "",
    var shortDescription: String = "",
    var instructions: String = "",
    var pictureUrl: String = "",
    var likedBy: List<String> = emptyList(),
    var lastUpdated: Long = 0
) {
    companion object {
        const val COLLECTION = "recipes"
        const val LAST_UPDATED = "lastUpdated"

        fun fromJson(json: Map<String, Any>): Recipe {
            val recipe = Recipe()
            recipe.id = json["id"] as? String ?: ""
            
            // Handle createdAt: could be Long (from toJson) or Timestamp
            recipe.createdAt = when (val createdAt = json["createdAt"]) {
                is Long -> createdAt
                is Timestamp -> createdAt.seconds * 1000 // Convert to millis if needed
                else -> 0L
            }
            
            recipe.userId = json["userId"] as? String ?: ""
            recipe.name = json["name"] as? String ?: ""
            recipe.shortDescription = json["shortDescription"] as? String ?: ""
            recipe.instructions = json["instructions"] as? String ?: ""
            recipe.pictureUrl = json["pictureUrl"] as? String ?: ""
            recipe.likedBy = json["likedBy"] as? List<String> ?: emptyList()
            
            // Handle lastUpdated: usually a Timestamp because of serverTimestamp()
            recipe.lastUpdated = when (val lastUpdated = json[LAST_UPDATED]) {
                is Long -> lastUpdated
                is Timestamp -> lastUpdated.seconds
                else -> 0L
            }
            return recipe
        }
    }

    fun toJson(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "createdAt" to createdAt,
            "userId" to userId,
            "name" to name,
            "shortDescription" to shortDescription,
            "instructions" to instructions,
            "pictureUrl" to pictureUrl,
            "likedBy" to likedBy,
            LAST_UPDATED to FieldValue.serverTimestamp()
        )
    }
}
