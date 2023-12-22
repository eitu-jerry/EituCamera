package com.eitu.eitucamera

import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FlashMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun EituCamera(
    flashType: FlashType = FlashType.AUTO,
    aspectRatio: Int = AspectRatio.RATIO_4_3,
    cameraState: EituCameraState = rememberEituCameraState(flashType = flashType),
    modifier: Modifier = Modifier,
    content : @Composable BoxScope.() -> Unit
) {

    val lifecycleOwner = LocalLifecycleOwner.current

    val animAspectRatio by animateFloatAsState(
        targetValue =
            if (aspectRatio == AspectRatio.RATIO_16_9) 9f / 16f
            else 3f / 4f,
        animationSpec = tween(durationMillis = 600)
        )

    LaunchedEffect(key1 = flashType) {
        cameraState.update(
            flashType = flashType
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(animAspectRatio)
            .then(modifier)
    ) {

        AndroidView(
            factory = {
                EituCamera(
                    cameraState = cameraState,
                    context = it,
                    lifecycleOwner = lifecycleOwner
                ).apply {
                    setAspectRatio(aspectRatio)
                } },
            modifier = Modifier
                .fillMaxSize()
        ) {
            it.setAspectRatio(aspectRatio)
        }

        content()
    }
}