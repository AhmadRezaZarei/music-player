package com.ray.zarei.musicplayer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ray.zarei.musicplayer.ui.theme.MusicPlayerTheme

class GrantAccessActivity : ComponentActivity() {


    private val permissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val launcherMultiplePermissions = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissionsMap ->
                val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
                if (areGranted) {
                    Toast.makeText(this, "Permission is granted", Toast.LENGTH_LONG).show()
                    navigateToMainActivity()
                    // Use location
                } else {
                    // Show dialog
                    Toast.makeText(this, "Permission is not granted", Toast.LENGTH_LONG).show()
                }
            }
            MusicPlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { checkAndRequestStoragePermission(this@GrantAccessActivity, permissions , launcherMultiplePermissions)  }) {
                            Text(text = "Grant Storage Access")
                        }
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this@GrantAccessActivity, MainActivity::class.java))
    }

    private fun checkAndRequestStoragePermission(
        context: Context,
        permissions: Array<String>,
        launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
    ) {

        if (
            permissions.all {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            // Use storage permissions
            Toast.makeText(this, " Permission already granted", Toast.LENGTH_LONG).show()
            navigateToMainActivity()
        } else {
            // Request permissions
            Log.e("GrantAccessActivity", "launch: called" )
            launcher.launch(permissions)
        }

    }


}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MusicPlayerTheme {
        Greeting("Android")
    }
}
