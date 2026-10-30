package com.runtrack.data.local

import java.io.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

data class TrackedShoe(
    val id: String,
    val brand: String,
    val model: String,
    val purchaseDate: String,
    val size: String? = null,
    val color: String? = null,
    val condition: String? = null,
    val openingDistanceKm: Double? = null,
    val openingRunCount: Int? = null,
    val notes: String? = null
) {
    val displayName: String get() = "$brand $model"
}

data class TrackedRun(
    val id: String,
    val startTime: String,
    val durationMinutes: Double,
    val distanceKm: Double,
    val paceMinutesPerKm: Double? = null,
    val heartRate: Int? = null,
    val calories: Double? = null,
    val shoeId: String? = null,
    val shoeName: String? = null,
    val averageSpeedKmh: Double? = null,
    val rating: String? = null,
    val temperatureC: Double? = null,
    val notes: String? = null
) {
    val effectivePace: Double get() = paceMinutesPerKm ?: durationMinutes / distanceKm
}

data class TrackSnapshot(
    val version: Int = 1,
    val shoes: List<TrackedShoe> = emptyList(),
    val runs: List<TrackedRun> = emptyList()
) {
    fun saveShoe(shoe: TrackedShoe) = copy(shoes = shoes.filterNot { it.id == shoe.id } + shoe)
    fun saveRun(run: TrackedRun) = copy(runs = runs.filterNot { it.id == run.id } + run)
    fun deleteRun(id: String) = copy(runs = runs.filterNot { it.id == id })
    // Preserve historical shoe names when the shoe itself is removed.
    fun deleteShoe(id: String) = copy(
        shoes = shoes.filterNot { it.id == id },
        runs = runs.map { if (it.shoeId == id) it.copy(shoeId = null) else it }
    )
    fun shoeDistance(shoe: TrackedShoe) = (shoe.openingDistanceKm ?: 0.0) +
        runs.filter { it.shoeId == shoe.id }.sumOf { it.distanceKm }
    fun shoeRunCount(shoe: TrackedShoe) = (shoe.openingRunCount ?: 0) + runs.count { it.shoeId == shoe.id }
    fun recentRuns() = runs.sortedByDescending { LocalDateTime.parse(it.startTime) }.take(3)
    fun weekSummary(today: LocalDate): WeekSummary {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val nextMonday = monday.plusWeeks(1)
        val week = runs.filter {
            val date = LocalDateTime.parse(it.startTime).toLocalDate()
            !date.isBefore(monday) && date.isBefore(nextMonday)
        }
        val distance = week.sumOf { it.distanceKm }
        val duration = week.sumOf { it.durationMinutes }
        return WeekSummary(distance, duration, if (distance > 0) duration / distance else null)
    }
}

data class WeekSummary(val distanceKm: Double, val durationMinutes: Double, val pace: Double?)

data class ShoeDraft(
    val brand: String = "", val model: String = "", val size: String = "",
    val color: String = "", val purchaseDate: String = LocalDate.now().toString(),
    val condition: String = "", val distance: String = "", val count: String = "", val notes: String = ""
) : Serializable {
    fun errors(): Map<String, String> = buildMap {
        if (brand.isBlank()) put("brand", "브랜드를 입력해 주세요.")
        if (model.isBlank()) put("model", "모델명을 입력해 주세요.")
        if (runCatching { LocalDate.parse(purchaseDate) }.isFailure) put("purchaseDate", "구매 날짜를 선택해 주세요.")
        if (distance.isNotBlank() && nonNegative(distance) == null) put("distance", "0 이상의 거리를 입력해 주세요.")
        if (count.isNotBlank() && (count.toIntOrNull() == null || count.toIntOrNull()!! < 0)) put("count", "0 이상의 정수를 입력해 주세요.")
    }
    fun toShoe(id: String = UUID.randomUUID().toString()): TrackedShoe {
        require(errors().isEmpty())
        return TrackedShoe(id, brand.trim(), model.trim(), purchaseDate, size.blankToNull(),
            color.blankToNull(), condition.blankToNull(), nonNegative(distance), count.toIntOrNull(), notes.blankToNull())
    }
    companion object {
        fun from(shoe: TrackedShoe) = ShoeDraft(shoe.brand, shoe.model, shoe.size.orEmpty(), shoe.color.orEmpty(),
            shoe.purchaseDate, shoe.condition.orEmpty(), shoe.openingDistanceKm?.let(::number).orEmpty(),
            shoe.openingRunCount?.toString().orEmpty(), shoe.notes.orEmpty())
    }
}

data class RunDraft(
    val startTime: String = LocalDateTime.now().withSecond(0).withNano(0).toString(),
    val duration: String = "", val distance: String = "", val pace: String = "",
    val heartRate: String = "", val calories: String = "", val shoeId: String = "",
    val speed: String = "", val rating: String = "", val temperature: String = "", val notes: String = ""
) : Serializable {
    fun errors(): Map<String, String> = buildMap {
        if (runCatching { LocalDateTime.parse(startTime) }.isFailure) put("startTime", "시작 날짜와 시간을 선택해 주세요.")
        if (positive(duration) == null) put("duration", "0보다 큰 러닝 시간을 입력해 주세요.")
        if (positive(distance) == null) put("distance", "0보다 큰 거리를 입력해 주세요.")
        if (pace.isNotBlank() && parsePace(pace) == null) put("pace", "6:09 또는 6.15 형식으로 입력해 주세요.")
        if (heartRate.isNotBlank() && (heartRate.toIntOrNull() == null || heartRate.toIntOrNull()!! <= 0)) put("heartRate", "0보다 큰 정수를 입력해 주세요.")
        if (calories.isNotBlank() && nonNegative(calories) == null) put("calories", "0 이상의 칼로리를 입력해 주세요.")
        if (speed.isNotBlank() && positive(speed) == null) put("speed", "0보다 큰 속도를 입력해 주세요.")
        if (temperature.isNotBlank() && temperature.toDoubleOrNull()?.isFinite() != true) put("temperature", "올바른 기온을 입력해 주세요.")
        if (rating.isNotBlank() && rating !in ratings) put("rating", "평가 항목을 선택해 주세요.")
    }
    fun toRun(shoes: List<TrackedShoe>, id: String = UUID.randomUUID().toString()): TrackedRun {
        require(errors().isEmpty())
        val shoe = shoes.find { it.id == shoeId }
        require(shoeId.isBlank() || shoe != null) { "선택한 신발을 찾을 수 없습니다." }
        return TrackedRun(id, startTime, positive(duration)!!, positive(distance)!!,
            parsePace(pace), heartRate.toIntOrNull(), nonNegative(calories), shoe?.id, shoe?.displayName,
            positive(speed), rating.blankToNull(), temperature.toDoubleOrNull(), notes.blankToNull())
    }
    companion object {
        val ratings = listOf("great", "good", "bad", "easy", "hard")
        fun from(run: TrackedRun) = RunDraft(run.startTime, number(run.durationMinutes), number(run.distanceKm),
            run.paceMinutesPerKm?.let(::paceText).orEmpty(), run.heartRate?.toString().orEmpty(),
            run.calories?.let(::number).orEmpty(), run.shoeId.orEmpty(), run.averageSpeedKmh?.let(::number).orEmpty(),
            run.rating.orEmpty(), run.temperatureC?.let(::number).orEmpty(), run.notes.orEmpty())
    }
}

private fun String.blankToNull() = trim().takeIf { it.isNotEmpty() }
fun nonNegative(value: String): Double? = value.trim().toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }
fun positive(value: String): Double? = nonNegative(value)?.takeIf { it > 0 }
fun parsePace(value: String): Double? {
    val parts = value.trim().split(':')
    if (parts.size == 1) return positive(value)
    if (parts.size != 2) return null
    val minutes = parts[0].toIntOrNull() ?: return null
    val seconds = parts[1].toIntOrNull() ?: return null
    return (minutes + seconds / 60.0).takeIf { minutes >= 0 && seconds in 0..59 && it > 0 }
}
fun paceText(pace: Double?): String {
    if (pace == null || !pace.isFinite() || pace <= 0) return "—"
    val seconds = (pace * 60).roundToInt()
    return "%d:%02d".format(Locale.US, seconds / 60, seconds % 60)
}
fun number(value: Double): String = if (value % 1.0 == 0.0) value.toLong().toString() else "%.2f".format(Locale.US, value).trimEnd('0').trimEnd('.')
