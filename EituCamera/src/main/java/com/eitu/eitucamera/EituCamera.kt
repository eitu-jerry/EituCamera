package com.eitu.eitucamera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.FrameLayout
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EituCamera(context : Context) : FrameLayout(context) {

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf (Manifest.permission.CAMERA).toTypedArray()
    }

    constructor(
        cameraState: EituCameraState,
        context: Context,
        lifecycleOwner: LifecycleOwner
    ) : this(context) {
        this.cameraState = cameraState
        this.lifecycleOwner = lifecycleOwner
        this.previewView = PreviewView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }

        init()
    }

    private val TAG : String by lazy { this.javaClass.simpleName }

    private lateinit var previewView : PreviewView

    private var imageCapture : ImageCapture? = null
    private lateinit var cameraState: EituCameraState
    private lateinit var lifecycleOwner : LifecycleOwner
    private lateinit var cameraExecutor: ExecutorService
    private var aspectRatio = AspectRatio.RATIO_16_9

    fun init() {
        this.addView(previewView)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        }
    }

    fun setAspectRatio(aspectRatio: Int) {
        this.aspectRatio = aspectRatio
        startCamera()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Image Capture
            imageCapture = ImageCapture.Builder()
                .setFlashMode(cameraState.getFlashMode())
                .setTargetAspectRatio(aspectRatio)
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )

                cameraState.imageCapture = imageCapture

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cameraExecutor.shutdown()
    }

}