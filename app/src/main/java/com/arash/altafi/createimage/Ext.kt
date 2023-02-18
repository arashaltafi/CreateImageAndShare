package com.arash.altafi.createimage

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.net.URL

fun Context.shareTextWithImage(
    applicationId: String,
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
            this, "$applicationId.fileprovider", file
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

fun Activity.shareTextWithVideo(
    applicationId: String,
    videoUrl: String,
    body: String,
    title: String,
    subject: String
) {
    val file = File(externalCacheDir, System.currentTimeMillis().toString() + ".mp4")

    Thread {
        URL(videoUrl).openStream().use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        runOnUiThread {
            val videoUri = if (Build.VERSION.SDK_INT < 24) {
                Uri.fromFile(file)
            } else {
                FileProvider.getUriForFile(
                    this, "$applicationId.fileprovider", file
                )
            }

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "video/mp4"
                putExtra(Intent.EXTRA_TEXT, title + "\n\n" + body)
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_STREAM, videoUri)
            }
            val chooserIntent = Intent.createChooser(sendIntent, "Share Video")
            chooserIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(chooserIntent)
        }
    }.start()
}

//////////////////////////////////////////////////////////////

@SuppressLint("Range")
fun Activity.downloadAndShareVideo(videoUrl: String) {
    // Create a File object from the external cache directory and the video file name
    val videoFile = File(externalCacheDir, System.currentTimeMillis().toString() + ".mp4")

    // Create a DownloadManager.Request object to start the download
    val downloadRequest = DownloadManager.Request(Uri.parse(videoUrl))
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
        .setTitle("Downloading video")
        .setDescription("Please wait...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationUri(Uri.fromFile(videoFile))

    // Get the DownloadManager system service
    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Enqueue the download request and get the download ID
    val downloadId = downloadManager.enqueue(downloadRequest)

    // Create a query to check the download status
    val downloadQuery = DownloadManager.Query().setFilterById(downloadId)

    // Start a background thread to check the download status
    Thread {
        var isDownloaded = false
        var downloadStatus = DownloadManager.STATUS_FAILED
        var downloadProgress = 0

        while (!isDownloaded) {
            // Get the download status from the DownloadManager
            val cursor = downloadManager.query(downloadQuery)
            if (cursor.moveToFirst()) {
                downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                downloadProgress =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                isDownloaded =
                    downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED
            }
            cursor.close()

            // Update the UI with the download progress
            runOnUiThread {
                // Update the UI with the download progress (e.g., using a progress bar)
            }

            // Wait for a short time before checking the download status again
            Thread.sleep(500)
        }

        // Show a toast message when the download is complete
        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
            runOnUiThread {
                Toast.makeText(this, "Video downloaded successfully", Toast.LENGTH_SHORT).show()
            }

            // Share the downloaded video file using an intent chooser
            shareVideoFile(videoFile)
        } else {
            runOnUiThread {
                Toast.makeText(this, "Video download failed", Toast.LENGTH_SHORT).show()
            }
        }
    }.start()
}

private fun Context.shareVideoFile(videoFile: File) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "video/mp4"
        putExtra(Intent.EXTRA_TEXT, "title" + "\n\n" + "body")
        putExtra(Intent.EXTRA_TITLE, "title")
        putExtra(Intent.EXTRA_SUBJECT, "subject")
        putExtra(
            Intent.EXTRA_STREAM,
            FileProvider.getUriForFile(this@shareVideoFile, "$packageName.fileprovider", videoFile)
        )
    }
    val chooserIntent = Intent.createChooser(sendIntent, "Share Video")
    chooserIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    startActivity(chooserIntent)
}