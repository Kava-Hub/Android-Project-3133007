package com.example.fitness_tracker.ui.onboarding

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitness_tracker.ui.onboarding.OnboardingState
import com.example.fitness_tracker.ui.onboarding.OnboardingStep
import com.example.fitness_tracker.ui.onboarding.OnboardingViewModel
import com.example.fitness_tracker.data.BiologicalSex
import com.example.fitness_tracker.domain.MoveGoalRules
import com.example.fitness_tracker.domain.ProfileDraft
import com.example.fitness_tracker.ui.components.ActivityRings
import com.example.fitness_tracker.ui.components.FitnessIcon
import com.example.fitness_tracker.ui.components.Glyph
import com.example.fitness_tracker.ui.theme.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun OnboardingRoute(model: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory)) {
    val state by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    var requesting by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        requesting = false
        model.finish()
    }
    BackHandler(state.step != OnboardingStep.Welcome && !requesting) { model.back() }
    OnboardingScreen(state, model::edit, model::selectGoal, model::adjustGoal, model::back, requesting) {
        focus.clearFocus()
        if (state.step != OnboardingStep.Notifications) model.next()
        else if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            model.finish()
        } else {
            requesting = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
internal fun OnboardingScreen(
    state: OnboardingState,
    edit: (ProfileDraft) -> Unit,
    selectGoal: (Int) -> Unit,
    adjustGoal: (Int) -> Unit,
    back: () -> Unit,
    requesting: Boolean = false,
    next: () -> Unit,
) {
    val background = if (state.step == OnboardingStep.Welcome) CardGrey else ScreenBlack
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(Modifier.fillMaxSize().background(background).safeDrawingPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 560.dp).fillMaxSize().padding(horizontal = 24.dp)) {
                Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (state.step != OnboardingStep.Welcome) {
                        TextButton(onClick = back, enabled = !requesting) {
                            FitnessIcon(Glyph.Back, FocusGreen, 18.dp)
                            Text("Back", Modifier.padding(start = 4.dp), color = FocusGreen)
                        }
                    } else Spacer(Modifier.width(16.dp))
                    Spacer(Modifier.weight(1f))
                    Text("${state.step.ordinal + 1} of 4", color = TextMuted, style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.semantics { contentDescription = "Setup step ${state.step.ordinal + 1} of 4" })
                }
                key(state.step) {
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 24.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        when (state.step) {
                            OnboardingStep.Welcome -> WelcomeContent()
                            OnboardingStep.Profile -> ProfileContent(state, edit)
                            OnboardingStep.MoveGoal -> MoveGoalContent(state.moveGoal, selectGoal, adjustGoal)
                            OnboardingStep.Notifications -> NotificationsContent()
                        }
                    }
                }
                Button(
                    onClick = next,
                    enabled = !requesting,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 28.dp).heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RaisedGrey, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Text(if (state.step == OnboardingStep.MoveGoal) "Set Move Goal" else "Continue",
                        style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun OnboardingTitle(title: String) {
    Text(title, Modifier.fillMaxWidth().semantics { heading() }, color = Color.White,
        fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.7).sp,
        textAlign = TextAlign.Center)
}

@Composable
private fun WelcomeContent() {
    Spacer(Modifier.height(26.dp))
    OnboardingTitle("Welcome to Fitness")
    Spacer(Modifier.height(58.dp))
    WelcomeFeature(Glyph.Rings, "See Your Activity", "Keep up with your rings, workouts, awards, and trends.")
    Spacer(Modifier.height(28.dp))
    WelcomeFeature(Glyph.People, "Share With Others", "Cheer on your friends as all of you close your rings.")
}

@Composable
private fun WelcomeFeature(icon: Glyph, title: String, detail: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        FitnessIcon(icon, FocusGreen, 42.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(detail, color = TextMuted, fontSize = 16.sp, lineHeight = 24.sp)
        }
    }
}

@Composable
private fun ProfileContent(state: OnboardingState, edit: (ProfileDraft) -> Unit) {
    val context = LocalContext.current
    val draft = state.draft
    var sexMenu by remember { mutableStateOf(false) }
    OnboardingTitle("Personalize Fitness\nand Health")
    Text("These details help personalize your fitness experience. They are saved only on this device.",
        Modifier.padding(top = 24.dp, bottom = 28.dp), textAlign = TextAlign.Center, fontSize = 16.sp, lineHeight = 24.sp)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardGrey).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val selectedDate = runCatching { LocalDate.parse(draft.birthDate) }.getOrNull()
        ProfilePickerRow("Date of Birth", selectedDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "Not set") {
            val date = selectedDate ?: LocalDate.now().minusYears(25)
            DatePickerDialog(context, { _, year, month, day ->
                edit(draft.copy(birthDate = LocalDate.of(year, month + 1, day).toString()))
            }, date.year, date.monthValue - 1, date.dayOfMonth).apply {
                datePicker.maxDate = System.currentTimeMillis()
                datePicker.minDate = LocalDate.now().minusYears(120).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }.show()
        }
        FieldError(state.errors["birthDate"])
        HorizontalDivider(color = RaisedGrey)
        Box {
            ProfilePickerRow("Sex", BiologicalSex.entries.firstOrNull { it.name == draft.sex }?.label ?: "Not set") { sexMenu = true }
            DropdownMenu(expanded = sexMenu, onDismissRequest = { sexMenu = false }) {
                BiologicalSex.entries.forEach { option ->
                    DropdownMenuItem(text = { Text(option.label) }, onClick = { edit(draft.copy(sex = option.name)); sexMenu = false })
                }
            }
        }
        FieldError(state.errors["sex"])
        HorizontalDivider(color = RaisedGrey)
        BiometricField("Height", "cm", draft.height, state.errors["height"], ImeAction.Next) { edit(draft.copy(height = it)) }
        BiometricField("Weight", "kg", draft.weight, state.errors["weight"], ImeAction.Done) { edit(draft.copy(weight = it)) }
    }
}

@Composable
private fun ProfilePickerRow(label: String, value: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button, onClick = onClick)
        .heightIn(min = 48.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontSize = 16.sp)
        Text(value, color = TextMuted, fontSize = 15.sp)
        FitnessIcon(Glyph.Chevron, TextMuted, 16.dp, Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun BiometricField(label: String, unit: String, value: String, error: String?, imeAction: ImeAction, onChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange = { if (it.length <= 7) onChange(it) }, modifier = Modifier.fillMaxWidth(),
        label = { Text(label) }, suffix = { Text(unit) }, singleLine = true, isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
        supportingText = error?.let { { Text(it) } }, shape = RoundedCornerShape(12.dp))
}

@Composable
private fun FieldError(error: String?) {
    if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveGoalContent(goal: Int, select: (Int) -> Unit, adjust: (Int) -> Unit) {
    Spacer(Modifier.height(26.dp))
    OnboardingTitle("Your Daily Move Goal")
    Text("Set a goal based on how active you are, or how active you’d like to be, each day.",
        Modifier.padding(top = 24.dp, bottom = 36.dp), textAlign = TextAlign.Center, fontSize = 16.sp, lineHeight = 24.sp)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        val presets = MoveGoalRules.presets.entries.toList()
        presets.forEachIndexed { index, (label, value) ->
            SegmentedButton(
                selected = goal == value,
                onClick = { select(value) },
                shape = SegmentedButtonDefaults.itemShape(index, presets.size),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                icon = {},
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color(0xFF555559), inactiveContainerColor = CardGrey,
                    activeContentColor = Color.White, inactiveContentColor = Color.White,
                    activeBorderColor = RaisedGrey, inactiveBorderColor = RaisedGrey,
                ),
            ) { Text(label, fontSize = 14.sp, textAlign = TextAlign.Center) }
        }
    }
    Spacer(Modifier.height(54.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        GoalAdjustButton("−", "Decrease Move goal by 10 kilocalories", goal > MoveGoalRules.MIN) { adjust(-1) }
        Text(goal.toString(), Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite }, fontSize = if (goal >= 1000) 52.sp else 68.sp,
            lineHeight = 80.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        GoalAdjustButton("+", "Increase Move goal by 10 kilocalories", goal < MoveGoalRules.MAX) { adjust(1) }
    }
    Text("KCAL / DAY", Modifier.padding(top = 12.dp), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun GoalAdjustButton(symbol: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick, enabled = enabled, modifier = Modifier.size(52.dp).semantics { contentDescription = description },
        shape = CircleShape, contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MovePink, contentColor = ScreenBlack)) {
        Text(symbol, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NotificationsContent() {
    Column(Modifier.widthIn(max = 310.dp).fillMaxWidth().height(270.dp)
        .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp))
        .background(Brush.verticalGradient(listOf(Color(0xFF252527), Color(0xFF38383A))))
        .padding(horizontal = 18.dp, vertical = 38.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("9:41", color = Color(0xFF96969A), fontSize = 58.sp, fontWeight = FontWeight.Light)
        Row(Modifier.padding(top = 20.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF555558)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActivityRings(listOf(0.82f, 0.74f, 0.9f), Modifier.size(40.dp), animate = false)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("ATHLIMA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("A little movement.\nA little closer to your goal.", color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
    Spacer(Modifier.height(30.dp))
    OnboardingTitle("Stay Motivated with\nFitness Notifications")
    Text("Notifications can help you close your rings, see your trends, and cheer on your friends.",
        Modifier.padding(top = 22.dp), color = TextMuted, fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.Center)
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun WelcomePreview() {
    Fitness_TrackerTheme { OnboardingScreen(OnboardingState(), {}, {}, {}, {}) {} }
}
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MoveGoalPreview() {
    Fitness_TrackerTheme { OnboardingScreen(OnboardingState(step = OnboardingStep.MoveGoal), {}, {}, {}, {}) {} }
}
