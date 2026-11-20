
package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun GalleryHeader(
    onBack: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            modifier = Modifier.size(24.dp),
                            tint = IntruderColors.TextPrimary
                        )
                    }
                }

                Text(
                    text = "Intruder",
                    style = IntruderTypography.Title,
                    color = IntruderColors.TextPrimary
                )
            }

            Surface(
                color = IntruderColors.SuccessContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Actif",
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    style = IntruderTypography.Label,
                    color = IntruderColors.Success
                )
            }
        }

        HorizontalDivider(
            color = IntruderColors.Border,
            thickness = 1.dp
        )
    }
}
