package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

// Shared by the gallery (multi-selection) and the detail screen (single event).
@Composable
fun DeleteEventsDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val plural = count > 1
    val buttonShape = RoundedCornerShape(12.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IntruderColors.Surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        title = {
            Text(
                text = if (plural) {
                    "Supprimer les événements ?"
                } else {
                    "Supprimer cet événement ?"
                },
                style = IntruderTypography.Title,
                color = IntruderColors.TextPrimary
            )
        },
        text = {
            Text(
                text = if (plural) {
                    "Les événements sélectionnés seront définitivement supprimés " +
                        "de votre galerie. Cette action est irréversible."
                } else {
                    "Cet événement sera définitivement supprimé de votre galerie. " +
                        "Cette action est irréversible."
                },
                style = IntruderTypography.Body,
                color = IntruderColors.TextSecondary
            )
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IntruderColors.SurfaceVariant,
                    contentColor = IntruderColors.PrimaryVariant
                )
            ) {
                Text("Annuler")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IntruderColors.Error,
                    contentColor = Color.White
                )
            ) {
                Text("Supprimer")
            }
        }
    )
}