package com.example.fitness_tracker.ui.summary

import android.graphics.ImageDecoder
import android.provider.Settings
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness_tracker.data.*
import com.example.fitness_tracker.domain.*
import com.example.fitness_tracker.ui.components.*
import com.example.fitness_tracker.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun AccountAvatar(account: AccountSettings, modifier: Modifier = Modifier) {
    val bitmap by produceState<android.graphics.Bitmap?>(null, account.avatar) {
        value = withContext(Dispatchers.IO) { account.avatar?.let { runCatching { android.graphics.BitmapFactory.decodeFile(it) }.getOrNull() } }
    }
    if (bitmap == null) Avatar(account.initials, modifier, palette = 3)
    else Image(bitmap!!.asImageBitmap(), "Profile photo", modifier.size(52.dp).clip(CircleShape), contentScale = ContentScale.Crop)
}

@Composable
internal fun ProfileScreen(account: AccountSettings, open: (String) -> Unit) {
    ScreenColumn {
        PageTitle("Account")
        Panel(Modifier.fillMaxWidth(), onClick = { open("identity") }) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AccountAvatar(account)
                Text(account.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                FitnessIcon(Glyph.Chevron, TextMuted)
            }
        }
        Panel(Modifier.fillMaxWidth(), onClick = { open("notifications") }) { AccountRow("Notifications") }
        Panel(Modifier.fillMaxWidth()) {
            listOf("Health Details" to "health", "Change Goals" to "goals", "Units of Measure" to "units").forEachIndexed { i, (label, route) ->
                if (i > 0) HorizontalDivider(color = RaisedGrey)
                Box(Modifier.fillMaxWidth().press { open(route) }.padding(vertical = 12.dp)) { AccountRow(label) }
            }
        }
    }
}
@Composable private fun AccountRow(label: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        FitnessIcon(Glyph.Chevron, TextMuted, 18.dp)
    }
}

@Composable
internal fun IdentityScreen(account: AccountSettings, save: (AccountSettings) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentAccount by rememberUpdatedState(account)
    var name by rememberSaveable { mutableStateOf(account.name) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var nameSaved by rememberSaveable { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            loading = true
            val result = withContext(Dispatchers.IO) { runCatching {
                AvatarStore(context).importPhoto(uri)
            } }
            result.onSuccess { path ->
                save(currentAccount.copy(avatar = path))
                currentAccount.avatar?.let { File(it).delete() }
            }.onFailure { error = "Unable to read this photo. Please choose another image." }
            loading = false
        }
    }
    ScreenColumn {
        PageTitle("Your Profile")
        AccountAvatar(account)
        OutlinedTextField(name, { name = it.take(60) }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        else ActionButton("Upload from library", { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, icon = Glyph.Plus)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (nameSaved) Text("Name saved", color = FocusGreen)
        if (account.avatar != null) ActionButton("Remove photo", { save(account.copy(avatar = null)); File(account.avatar).delete() })
        if (name.isNotBlank()) ActionButton("Save Name", { save(account.copy(name = name.trim())); error = null; nameSaved = true }, primary = true)
    }
}

@Composable
internal fun NotificationSettingsScreen(account: AccountSettings, save: (AccountSettings) -> Unit) {
    val context = LocalContext.current
    ScreenColumn {
        PageTitle("Notifications")
        Text("Android permission must also be enabled. Stand reminders are periodic reminders between 09:00 and 21:00; this phone does not yet detect sitting.", color = TextMuted)
        ActionButton("Android Notification Settings", {
            // implicit intent: Android resolves the appropriate system settings activity.
            context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
        })
        NotificationSwitch("Stand Reminders", "A gentle hourly reminder to get up and move.", account.standReminders) { save(account.copy(standReminders = it)) }
        NotificationSwitch("Goal Completions", "Celebrate completed rings and newly earned awards.", account.goalCompletions) { save(account.copy(goalCompletions = it)) }
        NotificationSwitch("Activity Sharing", "Updates from shared activity. Available when a sharing source is connected.", account.activitySharing) { save(account.copy(activitySharing = it)) }
    }
}
@Composable private fun NotificationSwitch(title: String, detail: String, selected: Boolean, change: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.animateContentSize()) {
        Panel(Modifier.fillMaxWidth(), onClick = { change(!selected) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Switch(selected, change, modifier = Modifier.semantics { contentDescription = title })
            }
        }
        Text(detail, color = TextMuted)
    }
}

@Composable
internal fun UnitsScreen(account: AccountSettings, save: (AccountSettings) -> Unit) {
    ScreenColumn {
        PageTitle("Units of Measure")
        MeasureUnits.entries.forEach { units ->
            Panel(Modifier.fillMaxWidth(), onClick = { save(account.copy(units = units)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (units == MeasureUnits.Metric) "Metric · cm, kg, km" else "Imperial · ft, lb, mi", Modifier.weight(1f))
                    RadioButton(account.units == units, { save(account.copy(units = units)) })
                }
            }
        }
        Text("Your stored measurements stay the same when you change units.", color = TextMuted)
    }
}

@Composable
private fun Wheel(values: List<String>, selected: Int, label: String, modifier: Modifier = Modifier, select: (Int) -> Unit) {
    val list = rememberLazyListState(selected.coerceIn(values.indices))
    val scope = rememberCoroutineScope()
    val currentSelect by rememberUpdatedState(select)
    val currentSelected by rememberUpdatedState(selected.coerceIn(values.indices))
    LaunchedEffect(selected, values.size) {
        if (!list.isScrollInProgress) list.animateScrollToItem(selected.coerceIn(values.indices))
    }
    LaunchedEffect(list, values.size) {
        snapshotFlow { list.isScrollInProgress }.drop(1).collect { scrolling ->
            if (!scrolling) {
                val center = (list.layoutInfo.viewportStartOffset + list.layoutInfo.viewportEndOffset) / 2
                list.layoutInfo.visibleItemsInfo.minByOrNull { kotlin.math.abs(it.offset + it.size / 2 - center) }
                    ?.let {
                        currentSelect(it.index)
                        delay(40)
                        if (it.index != currentSelected) list.animateScrollToItem(currentSelected)
                    }
            }
        }
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        LazyColumn(state = list, flingBehavior = rememberSnapFlingBehavior(list),
            modifier = Modifier.height(144.dp).fillMaxWidth().semantics { contentDescription = label },
            contentPadding = PaddingValues(vertical = 48.dp)) {
            itemsIndexed(values) { index, value ->
                Box(Modifier.fillMaxWidth().height(48.dp).background(if (index == selected) RaisedGrey else Color.Transparent)
                    .press { currentSelect(index); scope.launch { delay(40); list.animateScrollToItem(currentSelected) } }, contentAlignment = Alignment.Center) {
                    Text(value, color = if (index == selected) FocusGreen else TextMuted, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
internal fun HealthDetailsScreen(profile: UserProfile?, units: MeasureUnits, save: (UserProfile) -> Unit) {
    var year by rememberSaveable { mutableIntStateOf(profile?.dateOfBirth?.year ?: 2000) }
    var month by rememberSaveable { mutableIntStateOf(profile?.dateOfBirth?.monthValue ?: 1) }
    var day by rememberSaveable { mutableIntStateOf(profile?.dateOfBirth?.dayOfMonth ?: 1) }
    var sex by rememberSaveable { mutableStateOf(profile?.sex ?: BiologicalSex.NotSet) }
    var height by rememberSaveable { mutableDoubleStateOf(profile?.heightCm ?: 170.0) }
    var weight by rememberSaveable { mutableDoubleStateOf(profile?.weightKg ?: 65.0) }
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var saved by rememberSaveable { mutableStateOf(false) }
    val date = validBirthDate(year, month, day)
    LaunchedEffect(year, month) { day = date.dayOfMonth }
    val imperial = units == MeasureUnits.Imperial
    ScreenColumn {
        PageTitle("Personalize Fitness and Health")
        Text("These details are saved only on this device.", color = TextMuted)
        Panel(Modifier.fillMaxWidth()) {
            val rows = listOf("Date of Birth" to date.toString(), "Sex" to sex.label,
                "Height" to if (imperial) "${cmToInches(height).roundToInt() / 12} ft ${cmToInches(height).roundToInt() % 12} in" else "${if (height % 1.0 == 0.0) height.toInt().toString() else "%.1f".format(Locale.US, height)} cm",
                "Weight" to if (imperial) "${kgToPounds(weight).roundToInt()} lb" else "${if (weight % 1.0 == 0.0) weight.toInt().toString() else "%.1f".format(Locale.US, weight)} kg")
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) HorizontalDivider(color = RaisedGrey)
                Row(Modifier.fillMaxWidth().press { editing = label; saved = false }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, Modifier.weight(1f)); Text(value, color = TextMuted)
                }
            }
        }
        if (date > LocalDate.now()) Text("Date of birth cannot be in the future.", color = MaterialTheme.colorScheme.error)
        else ActionButton("Save Health Details", { save(UserProfile(date, sex, height, weight)); saved = true }, primary = true)
        if (saved) Text("Health details saved", color = FocusGreen, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
    }
    editing?.let { field ->
        AlertDialog(onDismissRequest = { editing = null }, title = { Text(field) }, confirmButton = { TextButton(onClick = { editing = null }) { Text("Done") } },
            text = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    when (field) {
                        "Date of Birth" -> {
                            Wheel((1..31).map(Int::toString), date.dayOfMonth - 1, "Day", Modifier.weight(1f)) { day = validBirthDate(year, month, it + 1).dayOfMonth }
                            Wheel(Month.entries.map { it.getDisplayName(TextStyle.FULL, Locale.ENGLISH) }, month - 1, "Month", Modifier.weight(1.6f)) { month = it + 1 }
                            Wheel((1876..2026).map(Int::toString), year - 1876, "Year", Modifier.weight(1f)) { year = it + 1876 }
                        }
                        "Sex" -> Wheel(BiologicalSex.entries.map { it.label }, sex.ordinal, "Sex", Modifier.weight(1f)) { sex = BiologicalSex.entries[it] }
                        "Height" -> if (!imperial) Wheel((30..275).map(Int::toString), height.roundToInt() - 30, "cm", Modifier.weight(1f)) { height = (it + 30).toDouble() }
                            else {
                                val inches = cmToInches(height).roundToInt().coerceIn(12, 108)
                                Wheel((1..9).map(Int::toString), inches / 12 - 1, "Feet", Modifier.weight(1f)) { height = inchesToCm(((it + 1) * 12 + inches % 12).coerceIn(12, 108)) }
                                Text("ft")
                                Wheel((0..11).map(Int::toString), inches % 12, "Inches", Modifier.weight(1f)) { height = inchesToCm((inches / 12 * 12 + it).coerceIn(12, 108)) }
                            }
                        "Weight" -> if (!imperial) Wheel((0..454).map(Int::toString), weight.roundToInt(), "kg", Modifier.weight(1f)) { weight = it.toDouble() }
                            else Wheel((0..900).map(Int::toString), kgToPounds(weight).roundToInt().coerceAtMost(900), "lb", Modifier.weight(1f)) { weight = poundsToKg(it) }
                    }
                }
            })
    }
}

@Composable
internal fun GoalScreen(goals: Goals, save: (Goals) -> Unit) {
    var move by rememberSaveable { mutableIntStateOf(goals.move) }
    var exercise by rememberSaveable { mutableIntStateOf(goals.exercise) }
    var stand by rememberSaveable { mutableIntStateOf(goals.stand) }
    ScreenColumn {
        PageTitle("Change Goals")
        GoalStepper("Move", move, "kcal / day", MovePink, 10, 5000, 10) { move = it }
        GoalStepper("Exercise", exercise, "min / day", ExerciseGreen, 5, 180, 5) { exercise = it }
        GoalStepper("Stand", stand, "hours / day", StandCyan, 1, 24, 1) { stand = it }
        ActionButton("Save Fitness Goals", { save(Goals(move, exercise, stand)) }, primary = true, icon = Glyph.Check)
    }
}
@Composable private fun GoalStepper(label: String, value: Int, unit: String, tint: Color, min: Int, max: Int, step: Int, change: (Int) -> Unit) {
    Panel(Modifier.fillMaxWidth()) {
        Text(label, color = tint, style = MaterialTheme.typography.headlineSmall)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            for (direction in listOf(-1, 0, 1)) {
                if (direction == 0) AnimatedContent(value, label = "Goal change") { Text(it.toString(), fontSize = 42.sp) }
                else Box(Modifier.size(52.dp).clip(CircleShape).background(tint)
                    .semantics { contentDescription = "${if (direction < 0) "Decrease" else "Increase"} $label goal" }
                    .press { change((value + direction * step).coerceIn(min, max)) }, contentAlignment = Alignment.Center) {
                    Text(if (direction < 0) "−" else "+", color = ScreenBlack, fontSize = 30.sp)
                }
            }
        }
        Text(unit, color = TextMuted)
    }
}
