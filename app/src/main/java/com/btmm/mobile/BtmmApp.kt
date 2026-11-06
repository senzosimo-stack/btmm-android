package com.btmm.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btmm.mobile.btmm.Assessment
import com.btmm.mobile.btmm.RuleStatus
import com.btmm.mobile.btmm.TdiCross
import com.btmm.mobile.btmm.Timeframe
import com.btmm.mobile.btmm.Verdict
import com.btmm.mobile.btmm.assess
import com.btmm.mobile.btmm.storage.SavedReading

private enum class BtmmTab(val title: String) {
    Dashboard("Dashboard"),
    SetupMonitor("Setup Monitor"),
    AnalysisTools("Analysis Tools"),
    TradeDesk("Trade Desk")
}

const val MANUAL_ONLY_TEXT =
    "This app does not place trades or connect to a broker. " +
        "The user makes every trading decision and executes " +
        "any order manually outside the app."

const val TRADE_DESK_TEXT =
    "No orders are sent. All decisions and execution remain manual, outside this app."

@Composable
fun BtmmApp(viewModel: ReadingsViewModel) {
    var selected by rememberSaveable { mutableStateOf(BtmmTab.Dashboard) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BtmmTab.entries.forEach { tab ->
                    NavigationBarItem(
                        modifier = Modifier.testTag("tab-${tab.name}"),
                        selected = selected == tab,
                        onClick = { selected = tab },
                        icon = { Text(tab.title.take(1)) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("BTMM", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(selected.title, style = MaterialTheme.typography.titleLarge)
                when (selected) {
                    BtmmTab.Dashboard -> DashboardScreen(viewModel)
                    BtmmTab.SetupMonitor -> SetupMonitorScreen(viewModel)
                    BtmmTab.AnalysisTools -> SavedReadingsScreen(
                        viewModel,
                        onEdit = { saved ->
                            viewModel.startEditing(saved)
                            selected = BtmmTab.SetupMonitor
                        }
                    )
                    BtmmTab.TradeDesk -> TradeDeskScreen(viewModel)
                }
            }
            ManualOnlyCard()
        }
    }
}

@Composable
private fun ManualOnlyCard() {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("manual-only-boundary")
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Manual-only", fontWeight = FontWeight.Bold)
            Text(MANUAL_ONLY_TEXT)
        }
    }
}

// ---------------------------------------------------------------- Dashboard

@Composable
private fun DashboardScreen(vm: ReadingsViewModel) {
    val readings by vm.readings.collectAsStateWithLifecycle()
    val latest = readings.firstOrNull()

    Card(Modifier.fillMaxWidth().testTag("dashboard-card")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (latest == null) {
                Text("No saved readings yet. Enter one in Setup Monitor.")
            } else {
                val a = vm.assessmentOf(latest)
                Text("Latest saved reading", fontWeight = FontWeight.Bold)
                Text(readingTitle(latest))
                Text(verdictTitle(a), modifier = Modifier.testTag("dashboard-verdict"))
                Text("Saved readings: ${readings.size}")
            }
            Text("Results are a rules check on values typed in by hand. They are not a trade signal.")
        }
    }
}

// ------------------------------------------------------------ Setup Monitor

@Composable
private fun SetupMonitorScreen(vm: ReadingsViewModel) {
    val form = vm.form
    val assessment = remember(form) { form.assess() }

    Text(
        if (vm.editingId == null) "Type the values from your chart. Leave a box empty if you do not have it."
        else "Editing a saved reading."
    )

    OutlinedTextField(
        value = form.symbol,
        onValueChange = { vm.updateForm(form.copy(symbol = it)) },
        label = { Text("Symbol") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("field-symbol")
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Timeframe.entries.forEach { tf ->
            FilterChip(
                selected = form.timeframe == tf,
                onClick = { vm.updateForm(form.copy(timeframe = tf)) },
                label = { Text(tf.name) },
                modifier = Modifier.testTag("tf-${tf.name}")
            )
        }
    }

    NumberField("Price", form.price, "field-price") { vm.updateForm(form.copy(price = it)) }
    NumberField("EMA 8", form.ema8, "field-ema8") { vm.updateForm(form.copy(ema8 = it)) }
    NumberField("EMA 8 (previous bar)", form.ema8Prev, "field-ema8prev") { vm.updateForm(form.copy(ema8Prev = it)) }
    NumberField("EMA 50", form.ema50, "field-ema50") { vm.updateForm(form.copy(ema50 = it)) }
    NumberField("EMA 200", form.ema200, "field-ema200") { vm.updateForm(form.copy(ema200 = it)) }
    NumberField("EMA 1200", form.ema1200, "field-ema1200") { vm.updateForm(form.copy(ema1200 = it)) }
    NumberField("RSI (0 to 100)", form.rsi, "field-rsi") { vm.updateForm(form.copy(rsi = it)) }

    Text("TDI cross")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(TdiCross.UP to "Up", TdiCross.DOWN to "Down", TdiCross.NONE to "None").forEach { (value, label) ->
            FilterChip(
                selected = form.tdiCross == value,
                onClick = { vm.updateForm(form.copy(tdiCross = value)) },
                label = { Text(label) },
                modifier = Modifier.testTag("tdi-${value.name}")
            )
        }
    }

    NumberField("MFI (0 to 100)", form.mfi, "field-mfi") { vm.updateForm(form.copy(mfi = it)) }
    NumberField("MFI (previous bar)", form.mfiPrev, "field-mfiprev") { vm.updateForm(form.copy(mfiPrev = it)) }
    NumberField("Spread (points)", form.spreadPoints, "field-spread") { vm.updateForm(form.copy(spreadPoints = it)) }

    Text("News lock")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = form.newsLocked == true,
            onClick = { vm.updateForm(form.copy(newsLocked = true)) },
            label = { Text("Locked") },
            modifier = Modifier.testTag("news-locked")
        )
        FilterChip(
            selected = form.newsLocked == false,
            onClick = { vm.updateForm(form.copy(newsLocked = false)) },
            label = { Text("Not locked") },
            modifier = Modifier.testTag("news-clear")
        )
    }

    OutlinedTextField(
        value = form.dailyPl,
        onValueChange = { vm.updateForm(form.copy(dailyPl = it)) },
        label = { Text("Daily P/L (negative = loss)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth().testTag("field-dailypl")
    )
    NumberField("Account balance", form.account, "field-account") { vm.updateForm(form.copy(account = it)) }
    NumberField("ATR (optional, for reference levels)", form.atr, "field-atr") { vm.updateForm(form.copy(atr = it)) }

    AssessmentCard(assessment)

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { vm.save() },
            enabled = assessment.verdict != Verdict.NO_DATA && assessment.verdict != Verdict.INVALID,
            modifier = Modifier.testTag("save-reading")
        ) { Text(if (vm.editingId == null) "Save reading" else "Update reading") }
        OutlinedButton(onClick = { vm.clearForm() }, modifier = Modifier.testTag("clear-form")) {
            Text(if (vm.editingId == null) "Clear" else "Cancel edit")
        }
    }

    vm.message?.let { Text(it, modifier = Modifier.testTag("message")) }
}

@Composable
private fun NumberField(label: String, value: String, tag: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag(tag)
    )
}

@Composable
private fun AssessmentCard(a: Assessment) {
    Card(Modifier.fillMaxWidth().testTag("assessment-card")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Rules check", fontWeight = FontWeight.Bold)
            Text(verdictTitle(a), style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("verdict"))
            Text("A check of the values you typed. It is not a trade signal and not advice.")

            a.errors.forEach { Text("Fix: $it", modifier = Modifier.testTag("error")) }

            if (a.missingInputs.isNotEmpty() && a.verdict != Verdict.INVALID) {
                Text("Not entered: ${a.missingInputs.joinToString(", ")}", modifier = Modifier.testTag("missing"))
            }
            a.bias?.let { Text("Bias: $it") }
            a.score?.let { Text("Score: $it") }

            a.rules.forEach { rule ->
                val status = when (rule.status) {
                    RuleStatus.PASS -> "PASS"
                    RuleStatus.FAIL -> "FAIL"
                    RuleStatus.MISSING -> "NOT CHECKED"
                }
                Text("R${rule.id} ${rule.name}: $status. ${rule.detail}", modifier = Modifier.testTag("rule-${rule.id}"))
            }
        }
    }
}

private fun verdictTitle(a: Assessment): String = when (a.verdict) {
    Verdict.NO_DATA -> "No data entered"
    Verdict.INVALID -> "Invalid input: no result"
    Verdict.INCOMPLETE -> "Incomplete: ${a.rulesNotChecked} of 9 rules not checked"
    Verdict.GATE_CLOSED -> "Gate closed: ${a.rulesPassed} of 9 rules passed"
    Verdict.GATE_OPEN -> "Gate open: all 9 rules passed"
}

// ------------------------------------------------------------ Saved readings

@Composable
private fun SavedReadingsScreen(vm: ReadingsViewModel, onEdit: (SavedReading) -> Unit) {
    val readings by vm.readings.collectAsStateWithLifecycle()

    Text("Saved readings", fontWeight = FontWeight.Bold)
    if (readings.isEmpty()) {
        Text("No saved readings yet.", modifier = Modifier.testTag("empty-readings"))
    }
    readings.forEach { saved ->
        Card(Modifier.fillMaxWidth().testTag("reading-${saved.id}")) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(readingTitle(saved), fontWeight = FontWeight.Bold)
                Text(verdictTitle(vm.assessmentOf(saved)))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onEdit(saved) }, modifier = Modifier.testTag("edit-${saved.id}")) {
                        Text("Edit")
                    }
                    OutlinedButton(onClick = { vm.delete(saved.id) }, modifier = Modifier.testTag("delete-${saved.id}")) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

private fun readingTitle(saved: SavedReading): String {
    val name = saved.reading.symbol.ifBlank { "(no symbol)" }
    val whenText = java.text.DateFormat.getDateTimeInstance().format(java.util.Date(saved.createdAt))
    return "$name ${saved.reading.timeframe.name}, $whenText"
}

// ---------------------------------------------------------------- Trade Desk

@Composable
private fun TradeDeskScreen(vm: ReadingsViewModel) {
    val assessment = remember(vm.form) { vm.form.assess() }
    val ref = assessment.reference

    Card(Modifier.fillMaxWidth().testTag("trade-desk-card")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(TRADE_DESK_TEXT)
        }
    }

    Card(Modifier.fillMaxWidth().testTag("reference-card")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Reference levels", fontWeight = FontWeight.Bold)
            if (ref == null) {
                Text(
                    "None. They appear only when the form in Setup Monitor has a Gate open result " +
                        "and an ATR value.",
                    modifier = Modifier.testTag("reference-none")
                )
            } else {
                Text("Worked out from your inputs. Not an order.")
                Text("Direction: ${ref.direction}")
                Text("Entry: ${price(ref.entry)}")
                Text("Stop loss: ${price(ref.stopLoss)}")
                Text("Take profit: ${price(ref.takeProfit)}")
                Text("Risk at 1% of account: ${price(ref.riskAmount)}")
                Text("Position size is not calculated. Work it out in your own platform.")
            }
        }
    }
}

private fun price(v: Double): String =
    java.math.BigDecimal(v).setScale(6, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
