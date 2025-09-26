package com.example.arquiprimerparcial.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryHelper {

    private const val CLOUD_NAME = "do9p2kjnq" // Cambia esto
    private const val API_KEY = "432889529428913" // Cambia esto
    private const val API_SECRET = "IFfz-gQSUSB7KOSFnvm3bUGms6w" // Cambia esto

    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to CLOUD_NAME,
            "api_key" to API_KEY,
            "api_secret" to API_SECRET
        )

        MediaManager.init(context, config)
    }

    suspend fun uploadImage(imageUri: Uri): String = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(imageUri)
            .unsigned("productos_preset)") // Crea un unsigned preset en Cloudinary
            .option("folder", "productos") // Carpeta donde se guardarán las imágenes
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Opcional: mostrar progreso
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Opcional: actualizar progreso
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) {
                        continuation.resume(url)
                    } else {
                        continuation.resumeWithException(Exception("No se pudo obtener la URL"))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception("Upload rescheduled: ${error.description}"))
                }
            })
            .dispatch()
    }
}