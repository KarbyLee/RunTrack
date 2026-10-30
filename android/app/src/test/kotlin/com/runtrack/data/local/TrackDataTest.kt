package com.runtrack.data.local

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class TrackDataTest {
    @Test fun shoeOnlyRequiresBrandModelAndDate() {
        assertTrue(ShoeDraft(brand = "Nike", model = "Pegasus", purchaseDate = "2026-10-30").errors().isEmpty())
        val shoe = ShoeDraft(brand = "Nike", model = "Pegasus").toShoe("shoe")
        assertNull(shoe.size); assertNull(shoe.color); assertNull(shoe.condition)
        assertNull(shoe.openingDistanceKm); assertNull(shoe.openingRunCount); assertNull(shoe.notes)
    }
    @Test fun invalidShoeFieldsAreRejected() {
        assertEquals(setOf("brand", "model", "purchaseDate", "distance", "count"),
            ShoeDraft(purchaseDate = "2026-02-30", distance = "NaN", count = "-1").errors().keys)
    }
    @Test fun runOnlyRequiresStartDurationAndDistance() {
        val draft = RunDraft(startTime = "2026-10-30T07:30", duration = "32", distance = "5.2")
        assertTrue(draft.errors().isEmpty())
        val run = draft.toRun(emptyList(), "run")
        assertNull(run.rating); assertNull(run.shoeId); assertNull(run.paceMinutesPerKm)
        assertEquals(32.0 / 5.2, run.effectivePace, 0.00001)
    }
    @Test fun optionalMetricsPersistWhenEntered() {
        val draft = RunDraft(duration = "32", distance = "5.2", pace = "6:09", heartRate = "145",
            calories = "320", speed = "9.75", rating = "great", temperature = "-2", notes = "가벼운 러닝")
        val run = draft.toRun(emptyList())
        assertEquals(6.15, run.paceMinutesPerKm!!, 0.00001)
        assertEquals(145, run.heartRate); assertEquals("great", run.rating)
        assertEquals(draft, RunDraft.from(run))
    }
    @Test fun badNumbersAndUnsupportedRatingsAreRejected() {
        val errors = RunDraft(startTime = "invalid", duration = "0", distance = "Infinity", pace = "6:90",
            heartRate = "2.5", calories = "-1", speed = "NaN", rating = "unknown", temperature = "NaN").errors()
        assertEquals(9, errors.size)
    }
    @Test fun allFiveRatingsAreAccepted() {
        RunDraft.ratings.forEach { assertTrue(RunDraft(duration = "10", distance = "2", rating = it).errors().isEmpty()) }
    }
    @Test fun weekIsMondayThroughSundayAndPaceUsesTotalDuration() {
        val data = TrackSnapshot(runs = listOf(
            run("before", "2026-10-25T23:59", 100.0, 500.0),
            run("monday", "2026-10-26T00:00", 2.0, 10.0),
            run("sunday", "2026-11-01T23:59", 8.0, 48.0),
            run("after", "2026-11-02T00:00", 100.0, 500.0)))
        val summary = data.weekSummary(LocalDate.of(2026, 10, 30))
        assertEquals(10.0, summary.distanceKm, 0.0)
        assertEquals(58.0, summary.durationMinutes, 0.0)
        assertEquals(5.8, summary.pace!!, 0.00001)
        assertNull(TrackSnapshot().weekSummary(LocalDate.now()).pace)
    }
    @Test fun recentThreeAreSortedByStartTime() {
        val data = TrackSnapshot(runs = listOf(run("1", "2026-10-30T08:00"), run("2", "2026-10-29T08:00"),
            run("3", "2026-10-30T09:00"), run("4", "2026-10-28T08:00")))
        assertEquals(listOf("3", "1", "2"), data.recentRuns().map { it.id })
    }
    @Test fun editsAndDeletesUpdateShoeTotalsWithoutDoubleCounting() {
        val shoe = ShoeDraft(brand = "Nike", model = "Pegasus", distance = "100", count = "20").toShoe("s")
        val original = run("r", "2026-10-30T08:00").copy(shoeId = "s", shoeName = shoe.displayName)
        val data = TrackSnapshot(shoes = listOf(shoe)).saveRun(original).saveRun(original.copy(distanceKm = 8.0))
        assertEquals(108.0, data.shoeDistance(shoe), 0.0)
        assertEquals(21, data.shoeRunCount(shoe))
        assertEquals(100.0, data.deleteRun("r").shoeDistance(shoe), 0.0)
        val deleted = data.deleteShoe("s")
        assertTrue(deleted.shoes.isEmpty()); assertNull(deleted.runs.single().shoeId)
        assertEquals(shoe.displayName, deleted.runs.single().shoeName)
    }
    @Test fun invalidPaceAndUnknownShoeCannotBeSaved() {
        assertNull(parsePace("-1:30")); assertNull(parsePace("6:60")); assertNull(parsePace("NaN"))
        assertEquals("6:00", paceText(5.999))
        assertThrows(IllegalArgumentException::class.java) {
            RunDraft(duration = "30", distance = "5", shoeId = "missing").toRun(emptyList())
        }
    }
    private fun run(id: String, time: String, km: Double = 5.0, minutes: Double = 30.0) = TrackedRun(id, time, minutes, km)
}
