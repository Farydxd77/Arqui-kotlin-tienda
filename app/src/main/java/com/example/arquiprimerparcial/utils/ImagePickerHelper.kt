package com.example.arquiprimerparcial.utils

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ImagePickerHelper(
    private val activity: AppCompatActivity,
    private val onImageSelected: (Uri) -> Unit
) {

    private val pickImageLauncher = activity.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    fun selectImage() {
        pickImageLauncher.launch("image/*")
    }
}