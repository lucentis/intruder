package fr.lucentis.intruder.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.theme.IntruderColors

@Composable
fun SettingsCategoryCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .background(
                color = IntruderColors.Surface,
                shape = RoundedCornerShape(16.dp)
            ),
        content = content
    )
}
