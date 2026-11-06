package com.btmm.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.btmm.mobile.btmm.storage.BtmmDatabase
import com.btmm.mobile.btmm.storage.ReadingRepository

class MainActivity : ComponentActivity() {

    private val viewModel: ReadingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReadingsViewModel(ReadingRepository(BtmmDatabase.get(applicationContext).readingDao())) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    BtmmApp(viewModel)
                }
            }
        }
    }
}
