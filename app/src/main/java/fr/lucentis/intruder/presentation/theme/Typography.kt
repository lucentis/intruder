package fr.lucentis.intruder.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Intruder typography scale, based on the system Roboto font.
 */
object IntruderTypography {
    val Display = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold
    )

    val Headline = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold
    )

    val Title = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    )

    val TitleSmall = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )

    val Body = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    )

    val BodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )

    val Label = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )

    val Caption = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal
    )
}
