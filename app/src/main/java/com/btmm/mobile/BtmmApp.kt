package com.btmm.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class BtmmTab(val title: String, val detail: String) {
    Dashboard("Dashboard", "Starter shell only; no readings or calculations yet."),
    SetupMonitor("Setup Monitor", "Rule evaluation is not implemented; no setup verdicts are generated."),
    AnalysisTools("Analysis Tools", "Tools will follow after behavior and rule sources are confirmed."),
    TradeDesk("Trade Desk", "No orders are sent. All decisions and execution remain manual, outside this app.")
}

@Composable
fun BtmmApp() {
    var selected by rememberSaveable { mutableStateOf(BtmmTab.Dashboard) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BtmmTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selected == tab,
                        onClick = { selected = tab },
                        icon = { Text(tab.title.take(1)) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        BtmmScreen(selected, padding)
    }
}

@Composable
private fun BtmmScreen(tab: BtmmTab, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "BTMM",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(tab.title, style = MaterialTheme.typography.titleLarge)

        Card(Modifier.fillMaxWidth()) {
            Text(
                text = tab.detail,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Card(
            Modifier
                .fillMaxWidth()
                .testTag("manual-only-boundary")
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Manual-only", fontWeight = FontWeight.Bold)
                Text(
                    "This app does not place trades or connect to a broker. " +
                        "The user makes every trading decision and executes " +
                        "any order manually outside the app."
                )
            }
        }
    }
}
