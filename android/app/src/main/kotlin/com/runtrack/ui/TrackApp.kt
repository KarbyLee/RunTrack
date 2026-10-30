package com.runtrack.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.runtrack.R
import com.runtrack.data.local.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TrackApp(vm: TrackViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf("home") }
    var editId by rememberSaveable { mutableStateOf<String?>(null) }
    var removeShoe by rememberSaveable { mutableStateOf<String?>(null) }
    var removeRun by rememberSaveable { mutableStateOf<String?>(null) }
    var settings by remember { mutableStateOf(false) }
    fun navigate(destination: String, id: String? = null) { editId = id; screen = destination }
    BackHandler(enabled = screen != "home" || state.saving) {
        if (!state.saving) screen = when (screen) { "shoeEditor" -> "shoes"; "runEditor" -> "runs"; else -> "home" }
    }
    Surface(Modifier.fillMaxSize()) {
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.loadFailed -> Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text(state.error ?: "기록을 불러오지 못했어요.")
                Button(onClick = { vm.reload() }) { Text("다시 불러오기") }
            }
            screen == "shoeEditor" -> ShoeEditor(state.data.shoes.find { it.id == editId }, state.saving,
                close = { navigate("shoes") }, save = { vm.saveShoe(it, editId) { navigate("shoes") } })
            screen == "runEditor" -> RunEditor(state.data.runs.find { it.id == editId }, state.data.shoes, state.saving,
                close = { navigate("runs") }, save = { vm.saveRun(it, editId) { navigate("runs") } })
            else -> Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when (screen) {
                        "runs" -> RunningScreen(state.data, { navigate("runEditor") }, { navigate("runEditor", it) }, { removeRun = it })
                        "shoes" -> ShoesScreen(state.data, { navigate("shoeEditor") }, { navigate("shoeEditor", it) }, { removeShoe = it })
                        "ai" -> AiScreen { navigate("home") }
                        else -> HomeScreen(state.data, { navigate("ai") }, { settings = true }, { navigate("runEditor") })
                    }
                }
                TrackNavigation(screen) { navigate(it) }
            }
        }
    }
    state.error?.takeUnless { state.loadFailed }?.let { message ->
        AlertDialog(onDismissRequest = vm::clearError, title = { Text("저장하지 못했어요") }, text = { Text(message) },
            confirmButton = { TextButton(onClick = vm::clearError) { Text("확인") } })
    }
    removeShoe?.let { id ->
        DeleteDialog("신발을 삭제할까요?", "러닝 기록은 유지되고, 과거 기록에는 당시 신발 이름이 남습니다.", { removeShoe = null }) {
            vm.deleteShoe(id); removeShoe = null
        }
    }
    removeRun?.let { id ->
        DeleteDialog("러닝 기록을 삭제할까요?", "주간 요약과 신발 누적 기록에도 반영됩니다.", { removeRun = null }) {
            vm.deleteRun(id); removeRun = null
        }
    }
    if (settings) AlertDialog(onDismissRequest = { settings = false }, title = { Text("기록 저장 안내") },
        text = { Text("기록은 이 기기에 저장됩니다. 앱을 삭제하면 저장한 기록도 삭제됩니다.") },
        confirmButton = { TextButton(onClick = { settings = false }) { Text("확인") } })
}

@Composable
fun HomeScreen(data: TrackSnapshot, ai: () -> Unit, settings: () -> Unit, add: () -> Unit) {
    val summary = data.weekSummary(LocalDate.now())
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(65.dp), contentAlignment = Alignment.Center) {
                AsyncImage("file:///android_asset/ellipse1.svg", contentDescription = null, modifier = Modifier.size(54.dp))
                Image(painterResource(R.drawable.profile_img), null, Modifier.size(65.dp), contentScale = ContentScale.Crop)
            }
            Text("RunTrack", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = settings, modifier = Modifier.testTag("settings")) {
                Image(painterResource(R.drawable.option_icon), "설정", Modifier.size(40.dp))
            }
        }
        Divider(color = MaterialTheme.colorScheme.outline)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("이번 주 요약", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = ai, modifier = Modifier.testTag("openAi")) { Text("AI 추천 →") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WeeklyMetric("총 거리", "${number(summary.distanceKm)} km", Modifier.weight(1f).testTag("weekDistance"))
                WeeklyMetric("총 러닝 시간", "${number(summary.durationMinutes)} 분", Modifier.weight(1f).testTag("weekDuration"))
                WeeklyMetric("평균 페이스", "${paceText(summary.pace)} /km", Modifier.weight(1f).testTag("weekPace"))
            }
            Text("최근 러닝 기록 3개", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (data.runs.isEmpty()) EmptyCard("아직 러닝 기록이 없어요.", "첫 러닝을 기록해 보세요.", "러닝 기록 추가", add)
            else data.recentRuns().forEach { run ->
                TrackCard {
                    Text(LocalDateTime.parse(run.startTime).format(DateTimeFormatter.ofPattern("M월 d일 · HH:mm")),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    Text("${distance(run.distanceKm)} km  ·  ${number(run.durationMinutes)}분  ·  ${paceText(run.effectivePace)} /km",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WeeklyMetric(label: String, value: String, modifier: Modifier) {
    Surface(modifier.heightIn(min = 96.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RunningScreen(data: TrackSnapshot, add: () -> Unit, edit: (String) -> Unit, delete: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val records = data.runs.sortedByDescending { it.startTime }.filter {
        query.isBlank() || listOf(it.startTime, it.shoeName.orEmpty(), it.notes.orEmpty(), it.rating.orEmpty()).any { value -> value.contains(query, true) }
    }
    Column {
        ScreenHeader("러닝 기록", "추가 +", action = add)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().testTag("searchRuns"), singleLine = true,
                placeholder = { Text("날짜·신발·메모 검색") })
            if (records.isEmpty()) EmptyCard(if (query.isBlank()) "아직 러닝 기록이 없어요." else "검색 결과가 없어요.",
                "날짜와 거리부터 간단히 기록해 보세요.", "러닝 기록 추가", add)
            records.forEach { run ->
                Text(LocalDateTime.parse(run.startTime).format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E) HH:mm", Locale.KOREAN)),
                    style = MaterialTheme.typography.titleMedium)
                TrackCard {
                    Text("🏃 ${distance(run.distanceKm)} km", style = MaterialTheme.typography.titleLarge)
                    Text("⏱ ${number(run.durationMinutes)}분")
                    run.shoeName?.let { Text("👟 $it") }
                    Text("⏱ 페이스: ${paceText(run.effectivePace)} /km")
                    run.heartRate?.let { Text("심박수: $it bpm") }
                    run.calories?.let { Text("칼로리: ${number(it)} kcal") }
                    run.averageSpeedKmh?.let { Text("평균 속도: ${number(it)} km/h") }
                    run.rating?.let { Text("러닝 후 평가: $it") }
                    run.temperatureC?.let { Text("🌡 ${number(it)} °C") }
                    run.notes?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    EditActions({ edit(run.id) }, { delete(run.id) })
                }
            }
        }
    }
}

@Composable
private fun ShoesScreen(data: TrackSnapshot, add: () -> Unit, edit: (String) -> Unit, delete: (String) -> Unit) {
    Column {
        ScreenHeader("신발 관리", "추가 +", action = add)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            if (data.shoes.isEmpty()) EmptyCard("등록된 신발이 없어요.", "함께 달리는 신발을 추가해 보세요.", "신발 추가", add)
            data.shoes.forEach { shoe ->
                Text(shoe.displayName, style = MaterialTheme.typography.titleLarge)
                TrackCard {
                    Text("브랜드: ${shoe.brand}")
                    Text("모델: ${shoe.model}")
                    shoe.size?.let { Text("사이즈: $it") }
                    shoe.color?.let { Text("색상: $it") }
                    Text("구매: ${shoe.purchaseDate}")
                    Divider()
                    Text("누적 주행: ${distance(data.shoeDistance(shoe))} km", fontWeight = FontWeight.Bold)
                    Text("러닝 횟수: ${data.shoeRunCount(shoe)}회")
                    shoe.condition?.let { Text("상태: $it") }
                    shoe.notes?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    EditActions({ edit(shoe.id) }, { delete(shoe.id) })
                }
            }
        }
    }
}

@Composable
private fun AiScreen(back: () -> Unit) {
    var message by remember { mutableStateOf(false) }
    Column {
        ScreenHeader("🤖 신발 추천", "새로고침", action = { message = true })
        Column(Modifier.verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("디자인 예시 · 실제 AI 추천이 아닙니다", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("1순위", style = MaterialTheme.typography.titleLarge)
            TrackCard {
                Text("👟 Nike Air Zoom Pegasus 41", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text("💡 추천 이유", style = MaterialTheme.typography.titleLarge)
                Text("가벼우면서 충분한 쿠셔닝을 제공하는 러닝화 예시입니다. 실제 추천 결과는 추천 서비스 연동 후 표시됩니다.")
                Spacer(Modifier.height(12.dp))
                Text("💰 가격대: 예시 $100–120")
                Text("👍 장점\n    · 가벼움\n    · 통풍성\n    · 내구성")
                Text("👎 단점\n    · 비용")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = { message = true }) { Text("더보기") }
                    Spacer(Modifier.width(10.dp))
                    OutlinedButton(onClick = { message = true }) { Text("구매가능") }
                }
            }
            TextButton(onClick = back) { Text("홈으로 돌아가기") }
        }
    }
    if (message) AlertDialog(onDismissRequest = { message = false }, title = { Text("추천 서비스 준비 중") },
        text = { Text("현재 화면은 디자인 예시입니다. 추천과 구매 정보는 서비스 연결 후 사용할 수 있어요.") },
        confirmButton = { TextButton(onClick = { message = false }) { Text("확인") } })
}

@Composable
internal fun ScreenHeader(title: String, actionLabel: String, enabled: Boolean = true, action: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().heightIn(min = 70.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = action, enabled = enabled, modifier = Modifier.testTag("headerAction")) { Text(actionLabel) }
        }
        Divider(color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
internal fun TrackNavigation(screen: String, navigate: (String) -> Unit) {
    Surface(shadowElevation = 3.dp) {
        Column {
            Divider(color = MaterialTheme.colorScheme.outline)
            Row(Modifier.fillMaxWidth().height(70.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                listOf(Triple("home", R.drawable.home_btn, "홈"), Triple("runs", R.drawable.clipboard_btn, "러닝 기록"),
                    Triple("shoes", R.drawable.shoes_btn, "신발 관리"), Triple("runEditor", R.drawable.plus_btn, "러닝 추가")).forEach { (route, image, label) ->
                    Box(modifier = Modifier.size(64.dp).semantics { selected = route == screen }
                        .clickable(role = Role.Button) { navigate(route) }.testTag("nav_$route"), contentAlignment = Alignment.Center) {
                        Image(painterResource(image), label, Modifier.size(when (route) { "runEditor" -> 44.dp; "runs" -> 54.dp; else -> 58.dp }))
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun EmptyCard(title: String, detail: String, actionLabel: String, action: () -> Unit) {
    TrackCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = action) { Text(actionLabel) }
    }
}

@Composable
private fun EditActions(edit: () -> Unit, delete: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        OutlinedButton(onClick = edit) { Text("편집") }
        Spacer(Modifier.width(10.dp))
        OutlinedButton(onClick = delete) { Text("삭제") }
    }
}

@Composable
private fun DeleteDialog(title: String, detail: String, dismiss: () -> Unit, confirm: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) }, text = { Text(detail) },
        confirmButton = { TextButton(onClick = confirm) { Text("삭제", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = dismiss) { Text("취소") } })
}

private fun distance(value: Double) = "%.2f".format(Locale.US, value)
