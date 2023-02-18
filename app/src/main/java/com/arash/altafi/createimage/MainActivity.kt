package com.arash.altafi.createimage

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.arash.altafi.createimage.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() = binding.apply {
        llBase.post {
            val bitmap = Bitmap.createBitmap(
                llBase.width,
                llBase.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            llBase.draw(canvas)

            btnNavigate.setOnClickListener {
                val uri = createUriFromBitmap(bitmap)
                val intent = Intent(this@MainActivity, SecondActivity::class.java)
                intent.putExtra("imageUri", uri.toString())
                startActivity(intent)
            }

            btnShare.setOnClickListener {
//                shareTextWithImage(packageName, bitmap, "body", "title", "subject")
//                shareTextWithVideo(
//                    packageName,
//                    "https://www.leader.ir/media/film/19/12/31912_364.mp4",
//                    "body",
//                    "title",
//                    "subject"
//                )
                downloadAndShareVideo("https://www.leader.ir/media/film/19/12/31912_364.mp4")

            }

            btnSave.setOnClickListener {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (
//                        ContextCompat.checkSelfPermission(this@MainActivity, WRITE_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED
//                    ) {
//                        requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 123)
//                    } else {
//                        saveImageToRoot(this@MainActivity, bitmap, "test")
//                    }
//                } else {
//                    saveImageToRoot(this@MainActivity, bitmap, "test")
//                }
//                if (
//                    ContextCompat.checkSelfPermission(this@MainActivity, WRITE_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED
//                ) {
//                    ActivityCompat.requestPermissions(
//                        this@MainActivity,
//                        arrayOf(WRITE_EXTERNAL_STORAGE), 123
//                    )
//                } else {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//                        saveToInternalStorageAndroid10Above(this@MainActivity, bitmap)
//                    else
//                        saveToInternalStorage(this@MainActivity, bitmap)

//                }
            }
        }
    }

    private fun createUriFromBitmap(bitmap: Bitmap): Uri? {
        val file = File(externalCacheDir, System.currentTimeMillis().toString() + ".jpg")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.close()
        return if (Build.VERSION.SDK_INT < 24) {
            Uri.fromFile(file)
        } else {
            FileProvider.getUriForFile(
                this@MainActivity, "$packageName.fileprovider", file
            )
        }
    }

    // Save a bitmap or a Uri to the "myFolder" directory in the root of the external storage
    private fun saveToInternalStorage(context: Context, input: Any) {
        val folderName = "myFolder"

        // Create the directory if it doesn't already exist
        val directory = File(Environment.getExternalStorageDirectory(), folderName)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Get the output file name and extension
        val extension = if (input is Bitmap) "png" else "jpg"
        val fileName = UUID.randomUUID().toString() + ".$extension"

        // Get the output file path
        val filePath = File(directory, fileName).absolutePath

        // Open an output stream to the file
        val outputStream: OutputStream = FileOutputStream(filePath)

        // Write the image data to the file
        when (input) {
            is Bitmap -> input.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            is Uri -> {
                val inputStream = context.contentResolver.openInputStream(input)
                BitmapFactory.decodeStream(inputStream)
                    ?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                inputStream?.close()
            }
        }

        // Close the output stream
        outputStream.flush()
        outputStream.close()

        // Scan the file so that it shows up in the gallery app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MediaScannerConnection.scanFile(context, arrayOf(filePath), null, null)
        } else {
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://${Environment.getExternalStorageDirectory()}")
                )
            )
        }

        // Show a toast message indicating that the file has been saved
        Toast.makeText(context, "File saved to $folderName/$fileName", Toast.LENGTH_SHORT).show()

    }

    private fun saveToInternalStorageAndroid10Above(context: Context, bitmap: Bitmap) {
        val resolver = context.contentResolver
        val imageName = "my_image.png"
        val imageDescription = "My image description"
        val imageCollection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val newImageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.DESCRIPTION, imageDescription)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
        }
        val newImageUri = resolver.insert(imageCollection, newImageDetails)
        resolver.openOutputStream(newImageUri!!).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
    }

    private fun saveImageToPictures(bitmap: Bitmap, uri: Uri?) {
        val folderName = "Pictures"
        val directory = when {
            uri != null -> {
                File(uri.path!!)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            }
            else -> {
                val picturesDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                File(picturesDirectory.absolutePath + File.separator + folderName)
            }
        }

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val extension = if (uri != null) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
        } else {
            "jpg"
        }
        val fileName = UUID.randomUUID().toString() + ".$extension"
        val filePath = File(directory, "fileName.png").absolutePath
        val outputStream = FileOutputStream(filePath)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        // Show a toast message indicating the save was successful
        Toast.makeText(this, "Image saved to $filePath", Toast.LENGTH_SHORT).show()
    }

}