package com.example.wildlife_app.ui.components

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
fun PhotoPicker(
    selectedPhotoUri: String?,
    onGalleryPhotoPicked: (Uri?) -> Unit,
    prepareCameraPhoto: () -> Uri?,
    onCameraResult: (Boolean) -> Unit,
    onCameraUnavailable: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    val context = LocalContext.current

    // Screen-only state, kept through rotation with rememberSaveable.
    var cameraPermissionDenied by rememberSaveable { mutableStateOf(false) }
    var cameraPermissionPermanentlyDenied by rememberSaveable { mutableStateOf(false) }

    // System Photo Picker: needs no permission. null means the user cancelled.
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> onGalleryPhotoPicked(uri) }

    // System camera app. false means cancelled or failed.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> onCameraResult(success) }

    val launchCamera: () -> Unit = {
        val target = prepareCameraPhoto()
        if (target != null) {
            try {
                cameraLauncher.launch(target)
            } catch (e: ActivityNotFoundException) {
                onCameraUnavailable()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraPermissionDenied = false
            cameraPermissionPermanentlyDenied = false
            launchCamera()
        } else {
            cameraPermissionDenied = true
            // After a denial, "no rationale" means Android won't show the prompt again.
            val activity = context.findActivity()
            cameraPermissionPermanentlyDenied = activity != null &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.CAMERA
                    )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Sighting Photo",
            style = MaterialTheme.typography.titleMedium
        )

        if (selectedPhotoUri != null) {
            AsyncImage(
                model = selectedPhotoUri,
                contentDescription = "Preview of the selected sighting photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            OutlinedButton(
                onClick = onRemovePhoto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remove Photo")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Gallery")
            }

            Button(
                onClick = {
                    val alreadyGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (alreadyGranted) {
                        cameraPermissionDenied = false
                        launchCamera()
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Camera")
            }
        }

        if (cameraPermissionDenied) {
            Text(
                text = "Camera permission was denied, so the camera can't be used. " +
                        "You can still choose a photo from the gallery.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            if (cameraPermissionPermanentlyDenied) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open app settings")
                }
            }
        }
    }
}