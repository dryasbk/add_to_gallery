package flowmobile.add_to_gallery
// package com.example.save_media_to_gallery

import android.app.Activity
import android.content.ContentResolver
import android.util.Log
import android.widget.Toast

import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
//
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
// class AddToGalleryPlugin : FlutterPlugin, MethodCallHandler {
class AddToGalleryPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {
// class SaveMediaToGalleryPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {

    private lateinit var context: Context

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        val channel = MethodChannel(flutterPluginBinding.binaryMessenger, "add_to_gallery")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        when (call.method) {
            "saveImage" -> {
                val imagePath = call.argument<String>("filePath")
                val albumName = call.argument<String>("albumName")
                val savedPath = saveImageToGallery(imagePath, albumName)
                result.success(savedPath)
            }
            "saveVideo" -> {
                val videoPath = call.argument<String>("filePath")
                val albumName = call.argument<String>("albumName")
                val savedPath = saveVideoToGallery(videoPath, albumName)
                result.success(savedPath)
            }
            else -> result.notImplemented()
        }
    }

    private fun saveImageToGallery(filePath: String?, albumName: String?): String? {
        val file = File(filePath ?: return null)
        if (!file.exists()) return null

        val bitmap = BitmapFactory.decodeFile(filePath)
        var fos: OutputStream? = null
        var savedPath: String? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$albumName")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                fos = context.contentResolver.openOutputStream(uri!!)
                savedPath = uri.toString()
            } else {
                val albumDir = File(context.getExternalFilesDir(null), albumName)
                if (!albumDir.exists()) albumDir.mkdirs()
                val destFile = File(albumDir, file.name)
                fos = FileOutputStream(destFile)
                MediaScannerConnection.scanFile(context, arrayOf(destFile.toString()), null, null)
                savedPath = destFile.absolutePath
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos?.flush()
            fos?.close()
            return savedPath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun saveVideoToGallery(filePath: String?, albumName: String?): String? {
        val file = File(filePath ?: return null)
        if (!file.exists()) return null

        var savedPath: String? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/$albumName")
                }
                val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                context.contentResolver.openOutputStream(uri!!)?.use {
                    it.write(file.readBytes())
                    it.flush()
                }
                savedPath = uri.toString()
            } else {
                val albumDir = File(context.getExternalFilesDir(null), albumName)
                if (!albumDir.exists()) albumDir.mkdirs()
                val destFile = File(albumDir, file.name)
                FileOutputStream(destFile).use { fos ->
                    fos.write(file.readBytes())
                    fos.flush()
                }
                MediaScannerConnection.scanFile(context, arrayOf(destFile.toString()), null, null)
                savedPath = destFile.absolutePath
            }
            return savedPath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        // Clean up resources if necessary
    }
}




/////////////////////////////////// original /////////////////////
// import android.app.Activity
// import android.content.ContentResolver
// import android.content.ContentValues
// import android.content.Context
// import android.database.Cursor
// import android.graphics.Bitmap
// import android.graphics.BitmapFactory
// import android.net.Uri
// import android.os.Build
// import android.os.Environment
// import android.provider.MediaStore
// import android.util.Log
// import android.widget.Toast
// import androidx.annotation.NonNull
// import io.flutter.embedding.engine.plugins.FlutterPlugin
// import io.flutter.plugin.common.MethodCall
// import io.flutter.plugin.common.MethodChannel
// import io.flutter.plugin.common.MethodChannel.MethodCallHandler
// import io.flutter.plugin.common.MethodChannel.Result
// import java.io.File
// import java.io.FileInputStream
// import java.io.FileOutputStream
// import java.io.InputStream

// class AddToGalleryPlugin : FlutterPlugin, MethodCallHandler {

//     private lateinit var context: Context
//     private lateinit var channel: MethodChannel

//     override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
//         context = binding.applicationContext
//         channel = MethodChannel(binding.binaryMessenger, "add_to_gallery")
//         channel.setMethodCallHandler(this)
//     }

//     override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
//         channel.setMethodCallHandler(null)
//     }

//     override fun onMethodCall(call: MethodCall, result: Result) {
//         if (call.method == "addToGallery") {
//             val album = call.argument<String>("album")!!
//             val path = call.argument<String>("path")!!
//             val contentResolver: ContentResolver = context.contentResolver;
//             try {
//                 if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//                     //
//                     // Android 9 and below
//                     //
//                     val filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                     val dir = File(filepath.absolutePath.toString() + "/$album/")
//                     if (!dir.exists()) {
//                         dir.mkdirs();
//                     }
//                     val file = File(dir, File(path).name)
//                     try {
//                         val output = FileOutputStream(file)
//                         val inS: InputStream = FileInputStream(File(path))
//                         val buf = ByteArray(1024)
//                         var len: Int
//                         while (inS.read(buf).also { len = it } > 0) {
//                             output?.write(buf, 0, len)
//                         }
//                         inS.close()
//                         output.close()
//                         // Copy image into  Gallery
//                         val values = ContentValues()
//                         values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
//                         values.put(MediaStore.Images.Media.MIME_TYPE, "images/*")
//                         values.put(MediaStore.MediaColumns.DATA, file.path)
//                         contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//                         result.success(file.path)
//                     } catch (e: java.lang.Exception) {
//                         e.printStackTrace()
//                         result.error("error", null, null)
//                     }
//                 } else {
//                     //
//                     // Android 10 and above
//                     //
//                     val value = ContentValues().apply {
//                         put(MediaStore.Images.Media.DISPLAY_NAME, File(path).name)
//                         put(MediaStore.Images.Media.MIME_TYPE, "images/*")
//                         put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/$album")
//                         put(MediaStore.Images.Media.IS_PENDING, 1)
//                     }
//                     val resolver = contentResolver
//                     val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//                     val item = resolver.insert(collection, value)
//                     if (item != null) {
//                         resolver.openOutputStream(item).use { out ->
//                             val inS: InputStream = FileInputStream(File(path))
//                             val buf = ByteArray(1024)
//                             var len: Int
//                             while (inS.read(buf).also { len = it } > 0) {
//                                 out?.write(buf, 0, len)
//                             }
//                             inS.close()
//                             out?.close()
//                         }
//                         value.clear()
//                         value.put(MediaStore.Images.Media.IS_PENDING, 0)
//                         resolver.update(item, value, null, null)
//                         result.success(getRealPathFromURI(context, item))
//                     } else {
//                         result.error("error", null, null)
//                     }
//                 }
//             } catch (e: Exception) {
//                 result.error("error", null, null)
//                 e.printStackTrace()
//             }
//         } else {
//             result.notImplemented()
//         }
//     }

//     fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
//         var cursor: Cursor? = null
//         return try {
//             val proj = arrayOf(MediaStore.Images.Media.DATA)
//             cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
//             val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//             cursor.moveToFirst()
//             cursor.getString(column_index)
//         } finally {
//             cursor?.close()
//         }
//     }
// }
///////////////////////////////////////// not working
// import android.app.Activity
// import android.content.ContentResolver
// import android.content.ContentValues
// import android.content.Context
// import android.database.Cursor
// import android.graphics.Bitmap
// import android.graphics.BitmapFactory
// import android.net.Uri
// import android.os.Build
// import android.os.Environment
// import android.provider.MediaStore
// import android.util.Log
// import android.widget.Toast
// import androidx.annotation.NonNull
// import io.flutter.embedding.engine.plugins.FlutterPlugin
// import io.flutter.plugin.common.MethodCall
// import io.flutter.plugin.common.MethodChannel
// import io.flutter.plugin.common.MethodChannel.MethodCallHandler
// import io.flutter.plugin.common.MethodChannel.Result
// import java.io.File
// import java.io.FileInputStream
// import java.io.FileOutputStream
// import java.io.InputStream


// // class AddToGalleryPlugin : FlutterPlugin, MethodCallHandler {

// //     private lateinit var context: Context
// //     private lateinit var channel: MethodChannel

// //     override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
// //         context = binding.applicationContext
// //         channel = MethodChannel(binding.binaryMessenger, "add_to_gallery")
// //         channel.setMethodCallHandler(this)
// //     }

// class AddToGalleryPlugin : FlutterPlugin, MethodCallHandler {

//     private lateinit var context: Context
//     private lateinit var channel: MethodChannel

//     override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
//         context = binding.applicationContext
//         channel = MethodChannel(binding.binaryMessenger, "add_to_gallery")
//         channel.setMethodCallHandler(this)
//     }

//     override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
//         channel.setMethodCallHandler(null)
//     }

//     override fun onMethodCall(call: MethodCall, result: Result) {
//         if (call.method == "addToGallery") {
//             val album = call.argument<String>("album")!!
//             val path = call.argument<String>("path")!!
//             val contentResolver: ContentResolver = context.contentResolver;
//             try {
//                 if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//                     // Android 9 and below
//                     val filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                     val dir = File(filepath.absolutePath.toString() + "/$album/")
//                     if (!dir.exists()) {
//                         dir.mkdirs();
//                     }
//                     val file = File(dir, File(path).name)
//                     try {
//                         val output = FileOutputStream(file)
//                         val inS: InputStream = FileInputStream(File(path))
//                         val buf = ByteArray(1024)
//                         var len: Int
//                         while (inS.read(buf).also { len = it } > 0) {
//                             output?.write(buf, 0, len)
//                         }
//                         inS.close()
//                         output.close()
//                         // Copy image into  Gallery
//                         val values = ContentValues()
//                         values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
//                         values.put(MediaStore.Images.Media.MIME_TYPE, getMimeType(path)) // Determine mime type based on file extension
//                         values.put(MediaStore.MediaColumns.DATA, file.path)
//                         contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//                         result.success(file.path)
//                     } catch (e: java.lang.Exception) {
//                         e.printStackTrace()
//                         result.error("error", null, null)
//                     }
//                 } else {
//                     // Android 10 and above
//                     val values = ContentValues().apply {
//                         put(MediaStore.Images.Media.DISPLAY_NAME, File(path).name)
//                         put(MediaStore.Images.Media.MIME_TYPE, getMimeType(path)) // Determine mime type based on file extension
//                         put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/$album")
//                         put(MediaStore.Images.Media.IS_PENDING, 1)
//                     }
//                     val resolver = contentResolver
//                     val collection = if (isVideo(path)) {
//                         MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//                     } else {
//                         MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//                     }
//                     val item = resolver.insert(collection, values)
//                     if (item != null) {
//                         resolver.openOutputStream(item).use { out ->
//                             val inS: InputStream = FileInputStream(File(path))
//                             val buf = ByteArray(1024)
//                             var len: Int
//                             while (inS.read(buf).also { len = it } > 0) {
//                                 out?.write(buf, 0, len)
//                             }
//                             inS.close()
//                             out?.close()
//                         }
//                         values.clear()
//                         values.put(MediaStore.Images.Media.IS_PENDING, 0)
//                         resolver.update(item, values, null, null)
//                         result.success(getRealPathFromURI(context, item))
//                     } else {
//                         result.error("error", null, null)
//                     }
//                 }
//             } catch (e: Exception) {
//                 result.error("error", null, null)
//                 e.printStackTrace()
//             }
//         } else {
//             result.notImplemented()
//         }
//     }

//     // Helper function to determine the MIME type based on file extension
//     private fun getMimeType(path: String): String {
//         val extension = File(path).extension
//         return when (extension) {
//             "jpg", "jpeg", "png" -> "image/jpeg"
//             "mp4", "avi", "mov" -> "video/mp4"
//             else -> "application/octet-stream" // Default mime type
//         }
//     }

//     // Helper function to check if the file is a video
//     private fun isVideo(path: String): Boolean {
//         val extension = File(path).extension
//         return extension in listOf("mp4", "avi", "mov")
//     }

//     // This method is deprecated. Use `MediaStore.MediaColumns.RELATIVE_PATH` instead.
//     @Deprecated("Use `MediaStore.MediaColumns.RELATIVE_PATH` instead.")
//     fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
//         var cursor: Cursor? = null
//         return try {
//             val proj = arrayOf(MediaStore.Images.Media.DATA)
//             cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
//             val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//             cursor.moveToFirst()
//             cursor.getString(column_index)
//         } finally {
//             cursor?.close()
//         }
//     }
// }