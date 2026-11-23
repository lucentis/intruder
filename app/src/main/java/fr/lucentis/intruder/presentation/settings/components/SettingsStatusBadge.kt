package fr.lucentis.intruder.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun SettingsStatusBadge(
    label: String,
    positive: Boolean = true
) {
    Text(
        text = label,
        modifier = Modifier
            .background(
                color = if (positive) {
                    IntruderColors.SuccessContainer
                } else {
                    IntruderColors.SurfaceVariant
                },
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        style = IntruderTypography.Caption,
        color = if (positive) {
            IntruderColors.Success
        } else {
            IntruderColors.TextSecondary
        }
    )
}
