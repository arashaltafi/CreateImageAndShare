package com.arash.altafi.createimage

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arash.altafi.createimage.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Second Activity"

        val uri = Uri.parse(intent.extras?.getString("imageUri"))

        binding.ivImage.setImageURI(uri)

    }
}