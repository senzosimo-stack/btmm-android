package com.btmm.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class ManualOnlyBoundaryTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun manualOnlyBoundaryIsVisibleOnStartup() {
        composeRule.setContent { BtmmApp() }
        composeRule.onNodeWithTag("manual-only-boundary").assertIsDisplayed()
        composeRule.onNodeWithText("Manual-only").assertIsDisplayed()
    }

    @Test
    fun tradeDeskStatesOrdersAreNotSent() {
        composeRule.setContent { BtmmApp() }
        composeRule.onNodeWithText("Trade Desk").performClick()
        composeRule.onNodeWithText(
            "No orders are sent. All decisions and execution remain manual, outside this app."
        ).assertIsDisplayed()
    }
}
