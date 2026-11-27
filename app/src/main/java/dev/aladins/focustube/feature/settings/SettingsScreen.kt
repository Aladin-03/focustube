package dev.aladins.focustube.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.BuildConfig
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.FtTopBar
import dev.aladins.focustube.core.designsystem.components.SettingsDivider
import dev.aladins.focustube.core.designsystem.components.SettingsGroup
import dev.aladins.focustube.core.designsystem.components.SettingsRow
import dev.aladins.focustube.core.designsystem.components.Tag
import dev.aladins.focustube.core.designsystem.components.TagStyle
import dev.aladins.focustube.core.designsystem.components.regionSummary
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.core.ui.RegionSheet
import dev.aladins.focustube.domain.model.QuotaStatus
import dev.aladins.focustube.domain.model.Region
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/** Settings (17): Videos (default regions, YouTube daily limit), Personas (archived, backup), version. */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onDefaultRegionsApply: (List<Region>) -> Unit,
    onOpenArchived: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = FocusTubeTheme.spacing
    var regionSheetOpen by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FtTopBar(title = stringResource(R.string.settings_title), onBack = onBack)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm)
        ) {
            SettingsGroup(label = stringResource(R.string.settings_group_videos)) {
                SettingsRow(
                    title = stringResource(R.string.settings_default_regions),
                    value = regionSummary(state.defaultRegions.map { it.code }),
                    onClick = { regionSheetOpen = true }
                )
                SettingsDivider()
                SettingsRow(
                    title = stringResource(R.string.settings_youtube_limit),
                    supporting = { state.quota?.let { QuotaDetails(it) } }
                )
            }
            Spacer(Modifier.height(spacing.xxl))
            SettingsGroup(label = stringResource(R.string.settings_group_personas)) {
                SettingsRow(
                    title = stringResource(R.string.settings_archived),
                    value = state.archivedCount.toString(),
                    onClick = onOpenArchived
                )
                SettingsDivider()
                SettingsRow(
                    title = stringResource(R.string.settings_backup),
                    trailing = { Tag(text = stringResource(R.string.tag_coming_later), style = TagStyle.ComingLater) }
                )
            }
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = FocusTubeTheme.typography.mono,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = spacing.xxl)
            )
        }
    }
    if (regionSheetOpen) {
        RegionSheet(
            title = stringResource(R.string.regions_default_title),
            selected = state.defaultRegions,
            defaults = Region.Defaults,
            onApply = {
                regionSheetOpen = false
                onDefaultRegionsApply(it)
            },
            onDismiss = { regionSheetOpen = false }
        )
    }
}

@Composable
private fun QuotaDetails(quota: QuotaStatus) {
    val numbers = NumberFormat.getIntegerInstance()
    Spacer(Modifier.height(FocusTubeTheme.spacing.sm))
    LinearProgressIndicator(
        progress = { (quota.usedToday.toFloat() / quota.dailyLimit).coerceIn(0f, 1f) },
        color = MaterialTheme.colorScheme.primary,
        trackColor = FocusTubeTheme.colors.meterOff,
        modifier = Modifier.fillMaxWidth().height(6.dp)
    )
    Spacer(Modifier.height(FocusTubeTheme.spacing.sm))
    Text(
        text = stringResource(
            R.string.settings_limit_used,
            numbers.format(quota.usedToday),
            numbers.format(quota.dailyLimit)
        ),
        style = FocusTubeTheme.typography.meta,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = stringResource(R.string.settings_limit_resets, localTime(quota.resetsAt)),
        style = FocusTubeTheme.typography.mono,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Reset time in the phone's time zone and format ("09:00" or "9:00 AM"). */
private fun localTime(instant: Instant): String = DateTimeFormatter.ofLocalizedTime(
    FormatStyle.SHORT
).withZone(ZoneId.systemDefault()).format(instant.toJavaInstant())

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    FocusTubeTheme {
        SettingsScreen(
            state = SettingsUiState(
                quota = QuotaStatus(1240, 10_000, Instant.fromEpochMilliseconds(0), false),
                archivedCount = 1
            ),
            onBack = {},
            onDefaultRegionsApply = {},
            onOpenArchived = {}
        )
    }
}
