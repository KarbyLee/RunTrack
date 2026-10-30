@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.runtrack.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.runtrack.data.local.*
import com.runtrack.ui.theme.RunTrackColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ShoeEditor(shoe: TrackedShoe?, busy: Boolean, close: () -> Unit, save: (ShoeDraft) -> Unit) {
    var draft by rememberSaveable(shoe?.id) { mutableStateOf(shoe?.let(ShoeDraft::from) ?: ShoeDraft()) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val errors = if (submitted) draft.errors() else emptyMap()
    val context = LocalContext.current
    EditorFrame(if (shoe == null) "신발 추가" else "신발 편집", busy, close, {
        submitted = true
        if (draft.errors().isEmpty()) save(draft)
    }) {
        FormField("브랜드 *", draft.brand, { draft = draft.copy(brand = it) }, "예: Nike", "brand", errors["brand"])
        FormField("모델명 *", draft.model, { draft = draft.copy(model = it) }, "예: Air Zoom Pegasus", "model", errors["model"])
        ChoiceField("사이즈 (선택)", draft.size, (200..330 step 5).map { "$it mm" }, "size") { draft = draft.copy(size = it) }
        FormField("색상 (선택)", draft.color, { draft = draft.copy(color = it) }, "예: Black", "color")
        PickerField("구매 날짜 *", draft.purchaseDate, "purchaseDate", errors["purchaseDate"]) {
            pickDate(context, LocalDate.parse(draft.purchaseDate)) { draft = draft.copy(purchaseDate = it.toString()) }
        }
        ChoiceField("신발 상태 (선택)", draft.condition, listOf("양호", "사용 중", "교체 권장", "폐기"), "condition") { draft = draft.copy(condition = it) }
        FormField("누적 거리 (km, 선택)", draft.distance, { draft = draft.copy(distance = it) }, "예: 120.5", "shoeDistance", errors["distance"], KeyboardType.Decimal,
            helper = "앱에 등록하기 전 누적 거리")
        FormField("러닝 횟수 (선택)", draft.count, { draft = draft.copy(count = it) }, "예: 24", "shoeCount", errors["count"], KeyboardType.Number,
            helper = "앱에 등록하기 전 러닝 횟수")
        FormField("메모 (선택)", draft.notes, { draft = draft.copy(notes = it) }, "신발에 대한 메모를 남겨보세요", "shoeNotes", multiline = true)
    }
}

@Composable
fun RunEditor(run: TrackedRun?, shoes: List<TrackedShoe>, busy: Boolean, close: () -> Unit, save: (RunDraft) -> Unit) {
    var draft by rememberSaveable(run?.id) { mutableStateOf(run?.let(RunDraft::from) ?: RunDraft()) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val errors = if (submitted) draft.errors() else emptyMap()
    val context = LocalContext.current
    EditorFrame(if (run == null) "러닝 기록 추가" else "러닝 기록 편집", busy, close, {
        submitted = true
        if (draft.errors().isEmpty()) save(draft)
    }) {
        PickerField("시작 날짜 · 시간 *", LocalDateTime.parse(draft.startTime).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), "startTime", errors["startTime"]) {
            val initial = LocalDateTime.parse(draft.startTime)
            pickDate(context, initial.toLocalDate()) { date ->
                TimePickerDialog(context, { _, hour, minute ->
                    draft = draft.copy(startTime = date.atTime(hour, minute).toString())
                }, initial.hour, initial.minute, true).show()
            }
        }
        FormField("러닝 시간 (분) *", draft.duration, { draft = draft.copy(duration = it) }, "예: 32", "duration", errors["duration"], KeyboardType.Decimal)
        FormField("거리 (km) *", draft.distance, { draft = draft.copy(distance = it) }, "예: 5.2", "runDistance", errors["distance"], KeyboardType.Decimal)
        FormField("평균 페이스 (분/km, 선택)", draft.pace, { draft = draft.copy(pace = it) }, "예: 6:09", "pace", errors["pace"], KeyboardType.Ascii)
        FormField("평균 심박수 (bpm, 선택)", draft.heartRate, { draft = draft.copy(heartRate = it) }, "예: 145", "heartRate", errors["heartRate"], KeyboardType.Number)
        FormField("칼로리 (kcal, 선택)", draft.calories, { draft = draft.copy(calories = it) }, "예: 320", "calories", errors["calories"], KeyboardType.Decimal)
        ChoiceField("신발 선택 (선택)", shoes.find { it.id == draft.shoeId }?.displayName.orEmpty(), shoes.map { it.displayName }, "runShoe",
            optionKeys = shoes.map { it.id }) { draft = draft.copy(shoeId = it) }
        FormField("평균 속도 (km/h, 선택)", draft.speed, { draft = draft.copy(speed = it) }, "예: 9.75", "speed", errors["speed"], KeyboardType.Decimal)
        ChoiceField("러닝 후 평가 (선택)", draft.rating, RunDraft.ratings, "rating") { draft = draft.copy(rating = it) }
        FormField("기온 (°C, 선택)", draft.temperature, { draft = draft.copy(temperature = it) }, "예: 22", "temperature", errors["temperature"], KeyboardType.Ascii)
        FormField("메모 (선택)", draft.notes, { draft = draft.copy(notes = it) }, "오늘의 러닝을 기록해보세요", "runNotes", multiline = true)
    }
}

@Composable
private fun EditorFrame(title: String, busy: Boolean, close: () -> Unit, save: () -> Unit, fields: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader(title, "닫기 ×", enabled = !busy, action = close)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("* 필수 · 나머지 항목은 선택 입력", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            fields()
        }
        Surface(shadowElevation = 4.dp) {
            Button(onClick = save, enabled = !busy, shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RunTrackColors.Accent, contentColor = RunTrackColors.Text),
                modifier = Modifier.fillMaxWidth().padding(18.dp).height(54.dp).testTag("save")) {
                if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("저장", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
internal fun FormField(label: String, value: String, change: (String) -> Unit, hint: String, tag: String,
                       error: String? = null, keyboard: KeyboardType = KeyboardType.Text, multiline: Boolean = false, helper: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        OutlinedTextField(value, change, modifier = Modifier.fillMaxWidth().testTag(tag),
            placeholder = { Text(hint) }, singleLine = !multiline, minLines = if (multiline) 3 else 1,
            isError = error != null, keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            supportingText = if (error != null || helper != null) {{ Text(error ?: helper.orEmpty()) }} else null)
    }
}

@Composable
private fun ChoiceField(label: String, selected: String, options: List<String>, tag: String, optionKeys: List<String> = options, change: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
            OutlinedTextField(selected.ifBlank { "선택 안 함" }, {}, readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth().testTag(tag),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
            ExposedDropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
                DropdownMenuItem(text = { Text("선택 안 함") }, onClick = { change(""); expanded = false })
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { change(optionKeys[index]); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun PickerField(label: String, value: String, tag: String, error: String?, pick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Box(Modifier.fillMaxWidth().clickable(onClick = pick).testTag(tag)) {
            OutlinedTextField(value, {}, enabled = false, modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Outlined.DateRange, contentDescription = "날짜 선택") },
                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline, disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface),
                isError = error != null)
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
    }
}

private fun pickDate(context: Context, initial: LocalDate, selected: (LocalDate) -> Unit) {
    DatePickerDialog(context, { _, year, month, day -> selected(LocalDate.of(year, month + 1, day)) },
        initial.year, initial.monthValue - 1, initial.dayOfMonth).show()
}
