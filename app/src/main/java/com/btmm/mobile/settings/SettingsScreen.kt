package com.btmm.mobile.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

private const val PREFS_NAME = "btmm_live_prefs"
private const val KEY_ENDPOINT = "endpoint_url"
private const val KEY_API_KEY = "api_key"

object LiveDataPrefs {
    fun getPrefs(context: android.content.Context) = EncryptedSharedPreferences.create(
        PREFS_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

@Composable
fun SettingsScreen(context: Context, padding: PaddingValues) {
    val prefs = remember { LiveDataPrefs.getPrefs(context) }
    var endpoint by remember { mutableStateOf(prefs.getString(KEY_ENDPOINT, "") ?: "") }
    var apiKey by remember { mutableStateOf(prefs.getString(KEY_API_KEY, "") ?: "") }
    var status by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Live data settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Credentials are stored encrypted on this device. They are never embedded in the app bundle.")

        OutlinedTextField(
            value = endpoint,
            onValueChange = { endpoint = it },
            label = { Text("Endpoint URL") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API key") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            prefs.edit().putString(KEY_ENDPOINT, endpoint).putString(KEY_API_KEY, apiKey).apply()
            status = "Saved encrypted credentials."
        }, modifier = Modifier.fillMaxWidth().testTag("save-settings")) {
            Text("Save credentials")
        }

        Button(onClick = {
            status = try {
                val url = endpoint.trim()
                if (url.isBlank()) {
                    "No endpoint configured."
                } else {
                    val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    val code = conn.responseCode
                    "Endpoint reachable: HTTP $code"
                }
            } catch (e: Exception) {
                "Verification failed: ${e.message}"
            }
        }, modifier = Modifier.fillMaxWidth().testTag("verify-settings")) {
            Text("Verify endpoint")
        }

        if (status.isNotBlank()) {
            Card(Modifier.fillMaxWidth().testTag("settings-status")) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = status, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Card(Modifier.fillMaxWidth().testTag("settings-boundary")) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Boundary", fontWeight = FontWeight.Bold)
                Text("No broker orders are sent from this app. Live data is read-only and optional.")
            }
        }
    }
}
