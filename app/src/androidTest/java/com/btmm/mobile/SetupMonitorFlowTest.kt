package com.btmm.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SetupMonitorFlowTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var env: TestEnv

    @Before fun setUp() { env = TestEnv() }
    @After fun tearDown() { env.close() }

    private fun open(tab: String) = rule.onNodeWithTag("tab-$tab").performClick()

    private fun type(tag: String, text: String) = rule.onNodeWithTag(tag).performTextInput(text)

    /** All nine rules pass, long. */
    private fun fillOpenLong(rsi: String = "55") {
        type("field-symbol", "EURUSD")
        type("field-price", "1.0850"); type("field-ema8", "1.0848"); type("field-ema8prev", "1.0846")
        type("field-ema50", "1.0830"); type("field-ema200", "1.0800"); type("field-ema1200", "1.0700")
        type("field-rsi", rsi)
        rule.onNodeWithTag("tdi-UP").performClick()
        type("field-mfi", "55"); type("field-mfiprev", "50"); type("field-spread", "20")
        rule.onNodeWithTag("news-clear").performClick()
        type("field-dailypl", "-100"); type("field-account", "10000"); type("field-atr", "0.0010")
    }

    private fun waitForTag(tag: String) =
        rule.waitUntil(5_000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }

    private fun waitForText(text: String) =
        rule.waitUntil(5_000) { rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }

    @Test
    fun emptyForm_showsNoData_andCannotBeSaved() {
        rule.setContent { BtmmApp(env.newViewModel()) }
        open("SetupMonitor")
        rule.onNodeWithTag("verdict").assertTextContains("No data entered")
        rule.onNodeWithTag("save-reading").assertIsNotEnabled()
    }

    @Test
    fun partialForm_isIncomplete_andUncheckedRulesAreSaidSo() {
        rule.setContent { BtmmApp(env.newViewModel()) }
        open("SetupMonitor")
        type("field-price", "1.0850")
        rule.onNodeWithTag("verdict").assertTextContains("Incomplete")
        rule.onNodeWithTag("rule-1").assertTextContains("NOT CHECKED", substring = true)
        rule.onNodeWithTag("missing").assertIsDisplayed()
    }

    @Test
    fun nonNumericText_isInvalid_withNoResult() {
        rule.setContent { BtmmApp(env.newViewModel()) }
        open("SetupMonitor")
        type("field-rsi", "abc")
        rule.onNodeWithTag("verdict").assertTextContains("Invalid input: no result")
        rule.onNodeWithTag("error").assertIsDisplayed()
        rule.onNodeWithTag("save-reading").assertIsNotEnabled()
    }

    @Test
    fun fullOpenReading_showsGateOpen_andReferenceLevelsOnTradeDesk() {
        rule.setContent { BtmmApp(env.newViewModel()) }
        open("SetupMonitor")
        fillOpenLong()
        rule.onNodeWithTag("verdict").assertTextContains("Gate open: all 9 rules passed")
        open("TradeDesk")
        rule.onNodeWithText("Direction: LONG").assertIsDisplayed()
        rule.onNodeWithText(TRADE_DESK_TEXT).assertIsDisplayed()
    }

    @Test
    fun failingRule_showsGateClosed_andNoReferenceLevels() {
        rule.setContent { BtmmApp(env.newViewModel()) }
        open("SetupMonitor")
        fillOpenLong(rsi = "85")
        rule.onNodeWithTag("verdict").assertTextContains("Gate closed: 8 of 9 rules passed")
        open("TradeDesk")
        rule.onNodeWithTag("reference-none").assertIsDisplayed()
    }

    @Test
    fun saveReviewEditDelete() {
        rule.setContent { BtmmApp(env.newViewModel()) }
        open("SetupMonitor")
        fillOpenLong()
        rule.onNodeWithTag("save-reading").assertIsEnabled().performClick()
        waitForText("Reading saved.")

        open("AnalysisTools")
        waitForTag("reading-1")
        rule.onNodeWithText("Gate open: all 9 rules passed").assertIsDisplayed()

        rule.onNodeWithTag("edit-1").performClick()
        rule.onNodeWithTag("field-price").assertTextContains("1.085")
        rule.onNodeWithTag("field-rsi").performTextReplacement("85")
        rule.onNodeWithTag("save-reading").performClick()
        waitForText("Reading updated.")

        open("AnalysisTools")
        waitForText("Gate closed: 8 of 9 rules passed")

        rule.onNodeWithTag("delete-1").performClick()
        waitForTag("empty-readings")
    }

    @Test
    fun savedReading_survivesReopeningTheApp() {
        val first = env.newViewModel()
        first.updateForm(
            com.btmm.mobile.btmm.FormState(
                symbol = "GBPUSD", price = "1.25", rsi = "50"
            )
        )
        first.save()
        runBlocking { env.repository.readings.first { it.isNotEmpty() } }

        rule.setContent { BtmmApp(env.newViewModel()) } // a fresh ViewModel, as after a restart
        open("AnalysisTools")
        waitForTag("reading-1")
        rule.onNodeWithTag("reading-1").assertIsDisplayed()
    }
}
