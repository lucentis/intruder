package fr.lucentis.intruder.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val IntruderLightColorScheme = lightColorScheme(
    primary = IntruderColors.Primary,
    onPrimary = IntruderColors.OnPrimary,
    primaryContainer = IntruderColors.PrimaryContainer,
    secondary = IntruderColors.PrimaryVariant,
    background = IntruderColors.Background,
    surface = IntruderColors.Surface,
    surfaceVariant = IntruderColors.SurfaceVariant,
    error = IntruderColors.Error,
    outline = IntruderColors.Border
)

private val IntruderMaterialTypography = Typography().copy(
    displayLarge = IntruderTypography.Display,
    headlineMedium = IntruderTypography.Headline,
    titleLarge = IntruderTypography.Title,
    titleMedium = IntruderTypography.TitleSmall,
    bodyLarge = IntruderTypography.Body,
    bodyMedium = IntruderTypography.BodyMedium,
    labelMedium = IntruderTypography.Label
)

/**
 * Intruder Material 3 theme.
 *
 * Screens can adopt it progressively.
 */
@Composable
fun IntruderTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IntruderLightColorScheme,
        typography = IntruderMaterialTypography,
        content = content
    )
}