package com.eitu.eitucamera

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.io.File

class EituCameraState(
    private val context: Context,
    flashType: FlashType
    ) {

    var imageCapture: ImageCapture? = null
    var camera : Camera? = null
    val aspectRatio : MutableState<Int> = mutableStateOf(AspectRatio.RATIO_4_3)
    val flashType = mutableStateOf(flashType)

    fun takePicture(
        file : File,
        onResult : (ImageCapture.OutputFileResults) -> Unit
    ) {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(file)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onResult(output)
                }
            }
        )
    }

    private fun changeFlashMode() {
        imageCapture?.flashMode = getFlashMode()
    }

    fun getFlashMode() : Int = when(flashType.value) {
        FlashType.OFF -> {
            ImageCapture.FLASH_MODE_OFF
        }
        FlashType.AUTO -> {
            ImageCapture.FLASH_MODE_AUTO
        }
        FlashType.ON -> {
            ImageCapture.FLASH_MODE_ON
        }
    }

    fun update(flashType: FlashType) {
        this.flashType.value = flashType
        changeFlashMode()
    }

}

@Composable
fun rememberEituCameraState(flashType: FlashType) : EituCameraState {
    val context = LocalContext.current
    return remember { EituCameraState(context, flashType) }
}