package com.runtrack.data.local

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime

class LocalTrackStore(context: Context) {
    private val preferences = context.getSharedPreferences("runtrack_records_v1", Context.MODE_PRIVATE)
    private val gson = Gson()

    suspend fun load(): TrackSnapshot = withContext(Dispatchers.IO) {
        val json = preferences.getString("snapshot", null) ?: return@withContext TrackSnapshot()
        try {
            val data = gson.fromJson(json, TrackSnapshot::class.java) ?: error("Empty snapshot")
            require(data.version == 1)
            require(data.shoes.map { it.id }.distinct().size == data.shoes.size)
            require(data.runs.map { it.id }.distinct().size == data.runs.size)
            data.shoes.forEach { LocalDate.parse(it.purchaseDate) }
            data.runs.forEach {
                LocalDateTime.parse(it.startTime)
                require(it.distanceKm.isFinite() && it.distanceKm > 0)
                require(it.durationMinutes.isFinite() && it.durationMinutes > 0)
            }
            data
        } catch (error: Exception) {
            // Never replace a damaged or newer snapshot with empty data.
            throw IOException("저장된 기록을 읽지 못했어요. 앱을 다시 열어 주세요.", error)
        }
    }

    suspend fun save(snapshot: TrackSnapshot) = withContext(Dispatchers.IO) {
        if (!preferences.edit().putString("snapshot", gson.toJson(snapshot)).commit()) {
            throw IOException("기록을 저장하지 못했어요. 저장 공간을 확인해 주세요.")
        }
    }
}
