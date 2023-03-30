package com.arash.altafi.createimage

import android.Manifest
import android.content.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arash.altafi.createimage.databinding.ActivityMainBinding
import com.arash.altafi.createimage.utils.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val registerStorageResult = PermissionUtils.register(
        this,
        object : PermissionUtils.PermissionListener {
            override fun observe(permissions: Map<String, Boolean>) {
                if (permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                    Toast.makeText(
                        this@MainActivity,
                        "permission storage is granted",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "permission storage is not granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

    private val registerStorageResultAndroid13 = PermissionUtils.register(
        this,
        object : PermissionUtils.PermissionListener {
            override fun observe(permissions: Map<String, Boolean>) {
                if (
                    permissions[Manifest.permission.READ_MEDIA_IMAGES] == true &&
                    permissions[Manifest.permission.READ_MEDIA_VIDEO] == true &&
                    permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "permission storage is granted",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "permission storage is not granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (
                        !PermissionUtils.isGranted(
                            this@MainActivity,
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO
                        )
                    ) {
                        PermissionUtils.requestPermission(
                            this@MainActivity, registerStorageResultAndroid13,
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO
                        )
                    } else {
                        saveImageFromBitmapToDownload(bitmap, "imageName.jpg")
                    }
                } else {
                    if (!PermissionUtils.isGranted(
                            this@MainActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                PermissionUtils.requestPermission(
                                    this@MainActivity, registerStorageResult,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            } else {
                                intentToSetting()
                            }
                        } else {
                            PermissionUtils.requestPermission(
                                this@MainActivity, registerStorageResult,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        }
                    } else {
                        saveImageFromBitmapToDownload(bitmap, "imageName.jpg")
                    }
                }
            }

            btnSaveImageToCache.setOnClickListener {
                saveImageFromBitmapToCache(bitmap, "imageName.jpg")
            }
        }
    }

    private fun intentToSetting() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
        )
    }

}