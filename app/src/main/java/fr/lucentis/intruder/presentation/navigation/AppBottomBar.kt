package fr.lucentis.intruder.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

private data class BottomBarItem(
    val label: String,
    val icon: ImageVector
)

private val BottomBarItems = listOf(
    BottomBarItem("Galerie", Icons.Outlined.PhotoLibrary),
    BottomBarItem("Paramètres", Icons.Outlined.Settings),
    BottomBarItem("Debug", Icons.Outlined.BugReport)
)

private val UnselectedColor = Color(0xFF77777A)

@Composable
fun AppBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.background(IntruderColors.Surface)) {
        HorizontalDivider(color = IntruderColors.Border)

        NavigationBar(
            containerColor = IntruderColors.Surface,
            tonalElevation = 0.dp
        ) {
            BottomBarItems.forEachIndexed { index, item ->
                val selected = selectedTab == index

                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = IntruderTypography.Label.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IntruderColors.Primary,
                        selectedTextColor = IntruderColors.Primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = UnselectedColor,
                        unselectedTextColor = UnselectedColor
                    )
                )
            }
        }
    }
}