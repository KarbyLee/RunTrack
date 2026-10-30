package com.runtrack.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.runtrack.data.local.TrackSnapshot
import com.runtrack.data.local.TrackedRun
import com.runtrack.ui.theme.RunTrackTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Preview(name = "홈 화면", showBackground = true, locale = "ko", widthDp = 393, heightDp = 780)
@Composable
private fun HomePreview() {
    val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val example = TrackSnapshot(runs = listOf(
        TrackedRun("preview-1", monday.atTime(7, 0).toString(), 31.0, 5.2),
        TrackedRun("preview-2", monday.plusDays(2).atTime(19, 0).toString(), 35.0, 6.0),
        TrackedRun("preview-3", monday.plusDays(4).atTime(7, 30).toString(), 32.0, 5.2)
    ))
    RunTrackTheme {
        Surface {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) { HomeScreen(example, {}, {}, {}) }
                TrackNavigation("home") {}
            }
        }
    }
}

@Preview(name = "신발 추가", showBackground = true, locale = "ko", widthDp = 393, heightDp = 780)
@Composable
private fun ShoeEditorPreview() {
    RunTrackTheme { Surface { ShoeEditor(null, false, {}, {}) } }
}

@Preview(name = "러닝 기록 추가", showBackground = true, locale = "ko", widthDp = 393, heightDp = 780)
@Composable
private fun RunEditorPreview() {
    RunTrackTheme { Surface { RunEditor(null, emptyList(), false, {}, {}) } }
}
