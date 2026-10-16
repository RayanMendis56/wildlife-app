
package com.example.wildlife_app.ui.components

import android.content.ActivityNotFoundException
import android.net.Uri
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun PhotoPicker(
    selectedPhotoUri: String?,
    onGalleryPhotoPicked: (Uri?) -> Unit,
    prepareCameraPhoto: () -> Uri?,
    onCameraResult: (Boolean) -> Unit,
    onCameraUnavailable: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> onGalleryPhotoPicked(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> onCameraResult(success) }

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
                    val target = prepareCameraPhoto()
                    if (target != null) {
                        try {
                            cameraLauncher.launch(target)
                        } catch (_: ActivityNotFoundException) {
                            onCameraUnavailable()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Camera")
            }
        }
    }
}
