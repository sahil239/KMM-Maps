package dev.sahildesai.maps

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

val Twilight = Color(0xFF14213D)
val SoftCream = Color(0xFFFFF8F0)
val GoldenOrange = Color(0xFFFFA500)
val Cyan = Color(0xFF7FDBFF)

fun Float.toOneDecimalString(): String {
    // e.g. 12.34 * 10 = 123.4 → roundToInt() = 123
    val multiplied = (this * 10).roundToInt()
    val integerPart = multiplied / 10
    val decimalPart = (multiplied % 10).absoluteValue
    return "$integerPart.$decimalPart"
}

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun provideAddressSearchApiKey(): String