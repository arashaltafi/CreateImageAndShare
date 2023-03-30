package com.arash.altafi.createimage

import android.content.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arash.altafi.createimage.databinding.ActivityMainBinding
import com.arash.altafi.createimage.utils.*

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

            btnShareImage.setOnClickListener {
                shareTextWithImage(bitmap, "body", "title", "subject")
            }

            btnSaveImageToDownload.setOnClickListener {
                saveImageFromBitmapToDownload(bitmap, "imageName.jpg")
            }

            btnSaveImageToCache.setOnClickListener {
                saveImageFromBitmapToCache(bitmap, "imageName.jpg")
            }
        }
    }

}