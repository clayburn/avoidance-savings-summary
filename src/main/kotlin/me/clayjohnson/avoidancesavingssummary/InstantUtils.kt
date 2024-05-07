package me.clayjohnson.avoidancesavingssummary

import java.time.Instant
import java.time.temporal.ChronoUnit

fun Instant.daysAgo(days: Int): Instant = this.minus(days.toLong(), ChronoUnit.DAYS)
fun Instant.minutesAgo(minutes: Int): Instant = this.minus(minutes.toLong(), ChronoUnit.MINUTES)