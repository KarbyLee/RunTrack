package com.runtrack.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.runtrack.MainActivity
import com.runtrack.data.local.LocalTrackStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun requiredFieldsSaveEditDeleteAndRecreation() {
        rule.onNodeWithTag("nav_shoes").performClick()
        rule.onNodeWithTag("headerAction").performClick()
        rule.onNodeWithTag("save").performClick()
        rule.onNodeWithText("브랜드를 입력해 주세요.").assertIsDisplayed()
        rule.onNodeWithTag("brand").performTextInput("테스트 브랜드")
        rule.onNodeWithTag("model").performTextInput("테스트 모델")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithTag("size").performScrollTo().performClick()
        rule.onNodeWithText("260 mm").performScrollTo().performClick()
        rule.onNodeWithTag("save").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("신발 관리").fetchSemanticsNodes().isNotEmpty() }
        val shoe = runBlocking { LocalTrackStore(rule.activity).load().shoes.single() }
        assertEquals("260 mm", shoe.size)
        assertNull(shoe.color); assertNull(shoe.condition); assertNull(shoe.openingDistanceKm)

        rule.onNodeWithTag("nav_runEditor").performClick()
        rule.onNodeWithTag("save").performClick()
        rule.onNodeWithText("0보다 큰 러닝 시간을 입력해 주세요.").assertIsDisplayed()
        rule.onNodeWithTag("duration").performTextInput("32")
        rule.onNodeWithTag("runDistance").performTextInput("5.2")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithTag("runShoe").performScrollTo().performClick()
        rule.onNodeWithText(shoe.displayName).performClick()
        rule.onNodeWithTag("rating").performScrollTo().performClick()
        rule.onNodeWithText("great").performClick()
        rule.onNodeWithTag("save").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("러닝 기록").fetchSemanticsNodes().isNotEmpty() }
        val saved = runBlocking { LocalTrackStore(rule.activity).load() }
        assertEquals(5.2, saved.shoeDistance(shoe), 0.00001)
        assertEquals(1, saved.shoeRunCount(shoe))
        assertEquals("great", saved.runs.single().rating)
        assertNull(saved.runs.single().heartRate)

        rule.onNodeWithTag("nav_home").performClick()
        rule.onNodeWithTag("weekDistance").assert(hasAnyDescendant(hasText("5.2 km")))
        rule.activityRule.scenario.recreate()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("weekDistance").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("weekDistance").assert(hasAnyDescendant(hasText("5.2 km")))
        rule.onNodeWithTag("openAi").performClick()
        rule.onNodeWithText("디자인 예시 · 실제 AI 추천이 아닙니다").assertIsDisplayed()
        rule.onNodeWithTag("nav_runs").performClick()
        rule.onNodeWithText("편집").performScrollTo().performClick()
        rule.onNodeWithTag("duration").performTextReplacement("42")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithTag("save").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("러닝 기록").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(42.0, runBlocking { LocalTrackStore(rule.activity).load().runs.single().durationMinutes }, 0.0)
        rule.onNodeWithText("삭제").performScrollTo().performClick()
        rule.onAllNodesWithText("삭제").onLast().performClick()
        rule.waitUntil(10000) { runBlocking { LocalTrackStore(rule.activity).load().runs.isEmpty() } }
        val deleted = runBlocking { LocalTrackStore(rule.activity).load() }
        assertEquals(0.0, deleted.shoeDistance(shoe), 0.0)
        assertEquals(0, deleted.shoeRunCount(shoe))
    }
}
