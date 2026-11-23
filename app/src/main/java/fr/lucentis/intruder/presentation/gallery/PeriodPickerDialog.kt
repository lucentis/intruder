package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// The Material date picker reads and returns days as epoch milliseconds at midnight UTC,
// whatever the device time zone is. Converting through UTC on both sides keeps the
// selected calendar day unchanged.
private fun LocalDate.toPickerMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toPickerDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodPickerDialog(
    initialPeriod: GalleryPeriod?,
    onDismiss: () -> Unit,
    onConfirm: (GalleryPeriod) -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialPeriod?.start?.toPickerMillis(),
        initialSelectedEndDateMillis = initialPeriod?.end?.toPickerMillis()
    )

    val startMillis = state.selectedStartDateMillis
    val endMillis = state.selectedEndDateMillis

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = startMillis != null,
                onClick = {
                    if (startMillis != null) {
                        val start = startMillis.toPickerDate()
                        // Only one day tapped: the period is that single day.
                        val end = endMillis?.toPickerDate() ?: start
                        onConfirm(GalleryPeriod(start, end))
                    }
                }
            ) {
                Text("Appliquer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    ) {
        // The range picker contains a lazy list, which needs a bounded height.
        DateRangePicker(
            state = state,
            modifier = Modifier.height(500.dp),
            showModeToggle = false
        )
    }
}