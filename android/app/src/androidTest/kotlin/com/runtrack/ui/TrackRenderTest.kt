package com.runtrack.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import org.hamcrest.CoreMatchers.endsWith
import com.runtrack.data.local.*
import com.runtrack.ui.theme.RunTrackTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@RunWith(AndroidJUnit4::class)
class TrackRenderTest {
    @get:Rule val rule = createComposeRule()

    @Test fun homeShowsThreeTotalsAndThreeRecentRuns() {
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val runs = listOf(TrackedRun("1", monday.atTime(7, 0).toString(), 31.0, 5.2),
            TrackedRun("2", monday.plusDays(2).atTime(19, 0).toString(), 35.0, 6.0),
            TrackedRun("3", monday.plusDays(4).atTime(7, 30).toString(), 32.0, 5.2))
        rule.setContent { RunTrackTheme { Surface { Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) { HomeScreen(TrackSnapshot(runs = runs), {}, {}, {}) }
            TrackNavigation("home") {}
        } } } }
        rule.onNodeWithTag("weekDistance").assert(hasAnyDescendant(hasText("16.4 km")))
        rule.onNodeWithTag("weekDuration").assert(hasAnyDescendant(hasText("98 분")))
        rule.onNodeWithTag("weekPace").assert(hasAnyDescendant(hasText("5:59 /km")))
        rule.onNodeWithText("최근 러닝 기록 3개").assertIsDisplayed()
        capture("home")
    }

    @Test fun shoeFormRendersOptionalFieldsAndWidgets() {
        rule.setContent { RunTrackTheme { Surface { ShoeEditor(null, false, {}, {}) } } }
        rule.onNodeWithTag("size").assertExists()
        rule.onNodeWithTag("purchaseDate").assertExists()
        capture("shoe-form-top")
        rule.onNodeWithTag("purchaseDate").performScrollTo().performClick()
        onView(withClassName(endsWith("DatePicker"))).check(matches(isDisplayed()))
        Espresso.pressBack()
        rule.onNodeWithTag("shoeNotes").performScrollTo()
        rule.onNodeWithText("러닝 횟수 (선택)").assertIsDisplayed()
        capture("shoe-form-bottom")
    }

    @Test fun runFormRendersRequiredAndOptionalFields() {
        rule.setContent { RunTrackTheme { Surface { RunEditor(null, emptyList(), false, {}, {}) } } }
        rule.onNodeWithTag("startTime").assertExists()
        capture("run-form-top")
        rule.onNodeWithTag("rating").performScrollTo()
        rule.onNodeWithTag("rating").assertIsDisplayed()
        capture("run-form-bottom")
    }

    private fun capture(name: String) {
        rule.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "review").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            rule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        File(directory, "$name-window.png").outputStream().use {
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
