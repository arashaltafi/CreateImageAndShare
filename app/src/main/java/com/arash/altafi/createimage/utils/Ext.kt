package com.arash.altafi.createimage.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun Context.createUriFromBitmap(bitmap: Bitmap): Uri? {
    val file = File(externalCacheDir, System.currentTimeMillis().toString() + ".jpg")
    val out = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    out.close()
    return if (Build.VERSION.SDK_INT < 24) {
        Uri.fromFile(file)
    } else {
        FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    }
}

fun Context.shareTextWithImage(
    bitmap: Bitmap,
    body: String,
    title: String,
    subject: String
) {
    val file = File(externalCacheDir, System.currentTimeMillis().toString() + ".jpg")
    val out = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    out.close()
    val bmpUri = if (Build.VERSION.SDK_INT < 24) {
        Uri.fromFile(file)
    } else {
        FileProvider.getUriForFile(
            this, "$packageName.fileprovider", file
        )
    }

    val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
    StrictMode.setVmPolicy(builder.build())

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/*"
        putExtra(Intent.EXTRA_TEXT, title + "\n\n" + body)
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_STREAM, bmpUri)
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Image")
    startActivity(shareIntent)
}

fun Context.saveImageFromBitmapToCache(bitmap: Bitmap, imageName: String) {
    val cacheDir = cacheDir
    val cacheFile = File(cacheDir, imageName)
    val fileOutputStream = FileOutputStream(cacheFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
    fileOutputStream.flush()
    fileOutputStream.close()

    // Show a toast indicating the file was saved
    val toastMessage = "Image saved to cache directory as $imageName"
    if (cacheFile.exists()) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
    }
}

fun Context.saveImageFromBitmapToDownload(bitmap: Bitmap, imageName: String) {
    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val downloadFile = File(downloadDir, imageName)

    //Check if exists, delete file
    if (downloadFile.exists()) {
        downloadFile.delete()
    }

    //SaveFile
    val downloadFileOutputStream = FileOutputStream(downloadFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, downloadFileOutputStream)
    downloadFileOutputStream.flush()
    downloadFileOutputStream.close()

    // Notify the system that a new file was added to the downloads directory
    MediaScannerConnection.scanFile(this, arrayOf(downloadFile.absolutePath), null, null)

    // Show a toast indicating the file was saved
    val toastMessage = "Image saved to downloads directory as $imageName"
    if (downloadFile.exists()) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
    }
}