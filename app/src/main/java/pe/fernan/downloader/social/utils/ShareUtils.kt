package pe.fernan.downloader.social.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

enum class PACKAGES(val _name: String, val value: String) {
    PACKAGE_WHATSAPP("WhatsApp", "com.whatsapp")
}
object ShareUtils {


    fun videoTo(videoFile: File, context: Context, customPackage: PACKAGES = PACKAGES.PACKAGE_WHATSAPP) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = getVideoFileExtension(context, videoFile) ?: "video/mp4"
            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", videoFile)
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage(customPackage.value)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }


        if (isAppInstalled(customPackage.value, context)) {
            context.startActivity(shareIntent)
        } else {
            Toast.makeText(context, "${customPackage._name} no está instalado", Toast.LENGTH_SHORT).show()
        }
    }


    fun getVideoFileExtension(context: Context, videoFile: File): String? {
        val contentResolver = context.contentResolver
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(videoFile.absolutePath)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
    }


    fun getVideoMimeType(context: Context, videoUri: Uri): String? {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(videoUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val mimeType = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE))
                return mimeType
            }
        }
        return null
    }


    fun isAppInstalled(packageName: String, context: Context): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}