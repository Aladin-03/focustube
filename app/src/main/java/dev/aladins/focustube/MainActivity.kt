package dev.aladins.focustube

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.navigation.AppScaffold
import dev.aladins.focustube.navigation.AppUiState
import dev.aladins.focustube.navigation.AppViewModel

/**
 * The single activity. Keeps the splash screen up until it knows whether to show Welcome or a
 * persona, then hosts the whole Compose app edge to edge.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { appViewModel.uiState.value is AppUiState.Loading }
        setContent {
            FocusTubeTheme {
                val appState by appViewModel.uiState.collectAsStateWithLifecycle()
                if (appState !is AppUiState.Loading) {
                    AppScaffold(appState = appState)
                }
            }
        }
    }
}
