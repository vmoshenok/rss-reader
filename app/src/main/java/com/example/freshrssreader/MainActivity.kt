package com.example.freshrssreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.freshrssreader.ui.navigation.NavGraph
import com.example.freshrssreader.ui.theme.FreshRSSReaderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreshRSSApp()
        }
    }
}

@Composable
fun FreshRSSApp() {
    val mainViewModel: MainViewModel = hiltViewModel()
    val themeMode by mainViewModel.themeMode.collectAsState()
    val startDestination by mainViewModel.startDestination.collectAsState()

    if (startDestination != null) {
        FreshRSSReaderTheme(themeMode = themeMode) {
            val navController = rememberNavController()
            NavGraph(
                navController = navController,
                startDestination = startDestination!!
            )
        }
    }
}
