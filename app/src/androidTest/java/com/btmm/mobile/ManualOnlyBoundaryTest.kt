package com.btmm.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertCountEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ManualOnlyBoundaryTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var env: TestEnv

    @Before fun setUp() { env = TestEnv() }
    @After fun tearDown() { env.close() }

    private val tabs = listOf("Dashboard", "SetupMonitor", "AnalysisTools", "TradeDesk")

    @Test
    fun manualOnlyBoundaryIsVisibleOnStartup() {
        composeRule.setContent { BtmmApp(env.newViewModel()) }
        composeRule.onNodeWithTag("manual-only-boundary").assertIsDisplayed()
        composeRule.onNodeWithText("Manual-only").assertIsDisplayed()
    }

    @Test
    fun manualOnlyBoundaryIsVisibleOnEveryTab() {
        composeRule.setContent { BtmmApp(env.newViewModel()) }
        tabs.forEach { tab ->
            composeRule.onNodeWithTag("tab-$tab").performClick()
            composeRule.onNodeWithTag("manual-only-boundary").assertIsDisplayed()
        }
    }

    @Test
    fun tradeDeskStatesOrdersAreNotSent() {
        composeRule.setContent { BtmmApp(env.newViewModel()) }
        composeRule.onNodeWithTag("tab-TradeDesk").performClick()
        composeRule.onNodeWithText(TRADE_DESK_TEXT).assertIsDisplayed()
    }

    @Test
    fun noTradingControlsAppearOnAnyTab() {
        composeRule.setContent { BtmmApp(env.newViewModel()) }
        tabs.forEach { tab ->
            composeRule.onNodeWithTag("tab-$tab").performClick()
            listOf("Buy", "Sell", "Place order", "Send order", "Connect broker").forEach { word ->
                composeRule.onAllNodes(hasText(word, substring = true, ignoreCase = true)).assertCountEquals(0)
            }
        }
    }
}
