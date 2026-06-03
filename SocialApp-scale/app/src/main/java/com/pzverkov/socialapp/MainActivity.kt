package com.pzverkov.socialapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.pzverkov.socialapp.core.navigation.SocialAppNavHost
import com.pzverkov.socialapp.core.ui.theme.SocialAppTheme
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelFactory = (application as SocialAppApplication).graph.viewModelFactory
        setContent {
            CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                SocialAppTheme {
                    SocialAppNavHost()
                }
            }
        }
    }
}
