package com.example.cookshare.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class LocalStore(private val context: Context) {

    fun saveImage(bitmap: Bitmap, imageName: String): String? {
        return try {
            val file = File(context.filesDir, imageName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            Log.d("LocalStore", "Image saved: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e("LocalStore", "Error saving image", e)
            null
        }
    }

    fun getImage(imageName: String): Bitmap? {
        val file = File(context.filesDir, imageName)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }
    
    fun getLocalImageUrl(imageName: String): String? {
        val file = File(context.filesDir, imageName)
        return if (file.exists()) {
            Uri.fromFile(file).toString()
        } else {
            null
        }
    }
}
