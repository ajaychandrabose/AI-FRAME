package com.example.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.example.model.PhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavedPhotoRepository(private val context: Context) {

    suspend fun saveBitmapToMediaStore(
        bitmap: Bitmap,
        sceneName: String = "AI Capture",
        compositionScore: Int = 85
    ): Uri? = withContext(Dispatchers.IO) {
        val resolver: ContentResolver = context.contentResolver
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "AIFrame_${timeStamp}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AIFrame")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            try {
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 96, outputStream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
                return@withContext imageUri
            } catch (e: Exception) {
                e.printStackTrace()
                // Cleanup on failure
                resolver.delete(imageUri, null, null)
                return@withContext null
            }
        }
        return@withContext null
    }

    suspend fun loadSavedPhotos(): List<PhotoItem> = withContext(Dispatchers.IO) {
        val photoList = mutableListOf<PhotoItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        try {
            val cursor = context.contentResolver.query(
                queryUri,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val widthColumn = it.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val heightColumn = it.getColumnIndex(MediaStore.Images.Media.HEIGHT)

                var count = 0
                while (it.moveToNext() && count < 60) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn) ?: "Photo"
                    val dateAdded = it.getLong(dateColumn) * 1000
                    val width = if (widthColumn >= 0) it.getInt(widthColumn) else 0
                    val height = if (heightColumn >= 0) it.getInt(heightColumn) else 0

                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    photoList.add(
                        PhotoItem(
                            uriString = contentUri.toString(),
                            displayName = name,
                            dateAddedMillis = dateAdded,
                            width = width,
                            height = height,
                            compositionScore = (78..96).random(),
                            sceneDetected = if (name.contains("AIFrame")) "Smart Composition" else "Captured"
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        photoList
    }

    suspend fun loadBitmapFromUri(uri: Uri, maxDimension: Int = 1920): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            inputStream?.close()

            var sampleSize = 1
            while (boundsOptions.outWidth / sampleSize > maxDimension || boundsOptions.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val finalStream = context.contentResolver.openInputStream(uri)
            val decoded = BitmapFactory.decodeStream(finalStream, null, decodeOptions)
            finalStream?.close()

            // Check orientation
            val rotated = decoded?.let { bmp ->
                try {
                    val exifStream = context.contentResolver.openInputStream(uri)
                    exifStream?.use { stream ->
                        val exif = ExifInterface(stream)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        val matrix = Matrix()
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        }
                        if (!matrix.isIdentity) {
                            Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                        } else bmp
                    } ?: bmp
                } catch (e: Exception) {
                    bmp
                }
            }
            return@withContext rotated
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun deletePhoto(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val rows = context.contentResolver.delete(uri, null, null)
            rows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
