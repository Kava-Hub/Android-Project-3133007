package com.example.fitness_tracker.ui.summary

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitness_tracker.data.*
import com.example.fitness_tracker.ui.components.*
import com.example.fitness_tracker.ui.theme.*
import java.time.LocalDate

@Composable
internal fun LogActivityScreen(records: List<com.example.fitness_tracker.domain.ActivityEntry>, save: (ActivityDay) -> Unit) {
    val context = LocalContext.current
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val existing = records.firstOrNull { it.day.date.toString() == date }?.day
    var move by rememberSaveable(date) { mutableStateOf((existing?.move ?: 0).toString()) }
    var exercise by rememberSaveable(date) { mutableStateOf((existing?.exercise ?: 0).toString()) }
    var stand by rememberSaveable(date) { mutableStateOf((existing?.stand ?: 0).toString()) }
    var steps by rememberSaveable(date) { mutableStateOf((existing?.steps ?: 0).toString()) }
    val imperial = LocalMeasureUnits.current == MeasureUnits.Imperial
    var distance by rememberSaveable(date) { mutableStateOf(((existing?.distance ?: 0.0) / if (imperial) 1.609344 else 1.0).toString()) }
    var error by remember { mutableStateOf(false) }
    ScreenColumn {
        PageTitle("Log Activity")
        Text("Enter your daily totals. Saving replaces this date’s totals; saved workout minutes are already included. These entries count toward awards.", color = TextMuted)
        ActionButton(date, {
            val d = LocalDate.parse(date)
            DatePickerDialog(context, { _, y, m, day -> date = LocalDate.of(y,m+1,day).toString() },d.year,d.monthValue-1,d.dayOfMonth)
                .apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        })
        for ((label, value, change) in listOf(Triple("Move (kcal)",move,{ v:String -> move=v }), Triple("Exercise (min)",exercise,{v:String -> exercise=v}), Triple("Stand (0–24 hours)",stand,{v:String -> stand=v}), Triple("Steps",steps,{v:String -> steps=v}), Triple(if (imperial) "Distance (mi)" else "Distance (km)",distance,{v:String -> distance=v}))) {
            OutlinedTextField(value, { if (it.length <= 9) change(it) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
        if (error) Text("Use non-negative numbers, at most 1,440 exercise minutes and 24 stand hours.", color = MaterialTheme.colorScheme.error)
        ActionButton("Save Activity", {
            val m=move.toIntOrNull(); val e=exercise.toIntOrNull(); val s=stand.toIntOrNull(); val st=steps.toIntOrNull(); val d=distance.replace(',','.').toDoubleOrNull()
            if (m==null || m<0 || e==null || e !in 0..1440 || s==null || s !in 0..24 || st==null || st<0 || d==null || !d.isFinite() || d<0) error=true
            else save(ActivityDay(LocalDate.parse(date),m,e,s,st,d * if (imperial) 1.609344 else 1.0))
        }, primary = true)
    }
}
