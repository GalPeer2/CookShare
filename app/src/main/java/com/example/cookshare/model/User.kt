package com.example.cookshare.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "",
    val email: String = "",
    val name: String = "",
    val profileImageUrl: String = "",
    val lastUpdated: Long = 0
) {
    companion object {
        const val COLLECTION = "users"
        const val LAST_UPDATED = "lastUpdated"

        fun fromJson(json: Map<String, Any>): User {
            val id = json["id"] as? String ?: ""
            val email = json["email"] as? String ?: ""
            val name = json["name"] as? String ?: ""
            val profileImageUrl = json["profileImageUrl"] as? String ?: ""
            val lastUpdated = json[LAST_UPDATED] as? Long ?: 0
            return User(id, email, name, profileImageUrl, lastUpdated)
        }
    }

    fun toJson(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "email" to email,
            "name" to name,
            "profileImageUrl" to profileImageUrl,
            LAST_UPDATED to lastUpdated
        )
    }
}