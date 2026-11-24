package fr.lucentis.intruder.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

private val SemiBoldLabel = IntruderTypography.Label.copy(fontWeight = FontWeight.SemiBold)

// Offset of the text column in a row: 40 dp icon tile + 12 dp gap. Notices under a row
// are aligned on it.
private val RowTextOffset = 52.dp

enum class BadgeKind { POSITIVE, NEUTRAL, WARNING }

internal fun FeatureState.badgeKind(): BadgeKind = when (this) {
    FeatureState.ACTIVE -> BadgeKind.POSITIVE
    FeatureState.INACTIVE -> BadgeKind.NEUTRAL
    FeatureState.WARNING -> BadgeKind.WARNING
}

internal fun FeatureState.badgeText(feminine: Boolean): String = when (this) {
    FeatureState.ACTIVE -> if (feminine) "Activée" else "Activé"
    FeatureState.INACTIVE -> if (feminine) "Désactivée" else "Désactivé"
    FeatureState.WARNING -> "Attention"
}

@Composable
fun StatusBadge(
    text: String,
    kind: BadgeKind,
    modifier: Modifier = Modifier
) {
    val container: Color
    val content: Color

    when (kind) {
        BadgeKind.POSITIVE -> {
            container = IntruderColors.SuccessContainer
            content = IntruderColors.Success
        }

        BadgeKind.NEUTRAL -> {
            container = IntruderColors.SurfaceVariant
            content = IntruderColors.TextSecondary
        }

        BadgeKind.WARNING -> {
            container = IntruderColors.WarningContainer
            content = IntruderColors.Warning
        }
    }

    Text(
        text = text,
        modifier = modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = SemiBoldLabel,
        color = content
    )
}

@Composable
fun SettingsTopBar(
    title: String,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Text(
                text = title,
                modifier = Modifier.padding(start = 4.dp),
                style = IntruderTypography.Title,
                color = IntruderColors.TextPrimary
            )
        }

        HorizontalDivider(color = IntruderColors.Border)
    }
}

// Frame shared by the six sub-pages: top bar, then a scrollable column of cards.
@Composable
fun SettingsSubScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SettingsTopBar(title = title, onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(IntruderColors.Surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                style = IntruderTypography.TitleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = IntruderColors.TextPrimary
            )
        }

        content()
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(color = IntruderColors.Border)
}

@Composable
fun SettingsIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    container: Color = IntruderColors.SurfaceVariant,
    tint: Color = IntruderColors.PrimaryVariant
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = tint
        )
    }
}

@Composable
private fun RowText(
    title: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = IntruderTypography.TitleSmall,
            color = IntruderColors.TextPrimary
        )

        if (description != null) {
            Text(
                text = description,
                style = IntruderTypography.Label,
                color = IntruderColors.TextSecondary
            )
        }
    }
}

@Composable
fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconTile(icon = icon)

        RowText(
            title = title,
            description = description,
            modifier = Modifier.weight(1f)
        )

        trailing?.invoke()

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = IntruderColors.TextTertiary
        )
    }
}

// Read-only row: the value is displayed but cannot be changed, so there is no chevron.
@Composable
fun SettingsValueRow(
    icon: ImageVector,
    title: String,
    description: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconTile(icon = icon)

        RowText(
            title = title,
            description = description,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = IntruderTypography.BodyMedium,
            color = IntruderColors.TextSecondary
        )
    }
}

// The whole row is the touch target and carries the switch semantics for TalkBack, so
// the Switch itself gets no click handler (onCheckedChange = null).
@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconContainer: Color = IntruderColors.SurfaceVariant,
    iconTint: Color = IntruderColors.PrimaryVariant
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconTile(
            icon = icon,
            container = iconContainer,
            tint = iconTint
        )

        RowText(
            title = title,
            description = description,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = IntruderColors.OnPrimary,
                checkedTrackColor = IntruderColors.Primary,
                checkedBorderColor = IntruderColors.Primary,
                uncheckedThumbColor = IntruderColors.OnPrimary,
                uncheckedTrackColor = IntruderColors.TextTertiary,
                uncheckedBorderColor = IntruderColors.TextTertiary
            )
        )
    }
}

@Composable
fun SettingsInfoCard(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(IntruderColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = IntruderColors.PrimaryVariant
            )

            Text(
                text = "À savoir",
                style = IntruderTypography.TitleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = IntruderColors.TextPrimary
            )
        }

        Text(
            text = text,
            style = IntruderTypography.Body,
            color = IntruderColors.TextSecondary
        )
    }
}

// Shown next to the feature that depends on an Android permission. When the permission is
// granted it is a quiet status line; otherwise a warning with the way to fix it. The
// state is always written, never conveyed by color alone.
@Composable
fun PermissionNotice(
    granted: Boolean,
    grantedLabel: String,
    warning: String,
    actions: @Composable RowScope.() -> Unit
) {
    if (granted) {
        Row(
            modifier = Modifier.padding(start = RowTextOffset, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = IntruderColors.Success
            )

            Text(
                text = grantedLabel,
                style = IntruderTypography.Label,
                color = IntruderColors.TextSecondary
            )
        }
    } else {
        Column(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(IntruderColors.WarningContainer)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = IntruderColors.Warning
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Attention",
                        style = SemiBoldLabel,
                        color = IntruderColors.Warning
                    )

                    Text(
                        text = warning,
                        style = IntruderTypography.Label,
                        color = IntruderColors.TextPrimary
                    )
                }
            }

            Row(
                modifier = Modifier.padding(start = 26.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                content = actions
            )
        }
    }
}

@Composable
fun WarningAction(
    label: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = IntruderColors.Warning
        )
    ) {
        Text(
            text = label,
            style = SemiBoldLabel
        )
    }
}