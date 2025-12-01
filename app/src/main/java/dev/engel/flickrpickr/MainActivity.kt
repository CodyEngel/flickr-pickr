package dev.engel.flickrpickr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.google.android.material.color.DynamicColors.isDynamicColorAvailable
import dagger.hilt.android.AndroidEntryPoint
import dev.engel.flickrpickr.core.ui.theme.FlickrTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDynamicColor = isDynamicColorAvailable()

        enableEdgeToEdge()
        setContent {
            FlickrTheme(isDynamicColor) {
                FlickrApp()
            }
        }
    }
}