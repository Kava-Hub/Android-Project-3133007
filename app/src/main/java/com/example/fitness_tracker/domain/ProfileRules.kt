package com.example.fitness_tracker.domain

import java.time.LocalDate
import java.time.YearMonth

internal fun validBirthDate(year: Int, month: Int, day: Int): LocalDate =
    LocalDate.of(year.coerceIn(1876, 2026), month.coerceIn(1, 12),
        day.coerceIn(1, YearMonth.of(year.coerceIn(1876, 2026), month.coerceIn(1, 12)).lengthOfMonth()))
internal fun cmToInches(cm: Double) = cm / 2.54
internal fun inchesToCm(inches: Int) = inches * 2.54
internal fun kgToPounds(kg: Double) = kg * 2.2046226218
internal fun poundsToKg(pounds: Int) = pounds / 2.2046226218
