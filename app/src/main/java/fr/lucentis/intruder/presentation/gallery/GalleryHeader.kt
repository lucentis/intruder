package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun GalleryHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Intruder",
                style = IntruderTypography.Headline,
                color = IntruderColors.TextPrimary
            )

            ProtectionBadge()
        }

        HorizontalDivider(
            color = IntruderColors.Border,
            thickness = 1.dp
        )
    }
}

// The dot and the label both carry the state, so it is not conveyed by color alone.
@Composable
private fun ProtectionBadge() {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(IntruderColors.SuccessContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(IntruderColors.Success, CircleShape)
        )

        Text(
            text = "Actif",
            style = IntruderTypography.Label.copy(fontWeight = FontWeight.SemiBold),
            color = IntruderColors.Success
        )
    }
}