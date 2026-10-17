package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.remote.CurrencyApiService
import com.example.data.remote.NetworkMonitor
import com.example.data.repository.CurrencyRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.ValutaKoersTheme
import com.example.ui.viewmodel.CurrencyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CurrencyViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val apiService = CurrencyApiService.create()
        val networkMonitor = NetworkMonitor(applicationContext)
        val locationDetector = com.example.data.location.LocationDetector(applicationContext)
        val repository = CurrencyRepository(database, apiService, networkMonitor)
        CurrencyViewModel.Factory(repository, locationDetector)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            ValutaKoersTheme(themeMode = uiState.themeMode) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
