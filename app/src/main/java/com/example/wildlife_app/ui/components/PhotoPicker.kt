
package com.example.wildlife_app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun PhotoPicker(
    selectedPhotoUri: String?,
    onPhotoSelected: (String?) -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // A null URI means the user cancelled the picker.
        if (uri != null) {
            onPhotoSelected(uri.toString())
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
            Text(
                text = "A photo has been selected.",
                modifier = Modifier.semantics {
                    contentDescription = "A photo has been selected"
                }
            )

            OutlinedButton(
                onClick = { onPhotoSelected(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remove Photo")
            }
        }

        Button(
            onClick = { photoPickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (selectedPhotoUri == null) {
                    "Choose Photo"
                } else {
                    "Choose Another Photo"
                }
            )
        }
    }
}
