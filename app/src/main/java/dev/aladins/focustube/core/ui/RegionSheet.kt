package dev.aladins.focustube.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.R
import dev.aladins.focustube.core.common.format.countryName
import dev.aladins.focustube.core.common.format.regionLabel
import dev.aladins.focustube.core.designsystem.components.FtSearchField
import dev.aladins.focustube.core.designsystem.components.FtTextButton
import dev.aladins.focustube.core.designsystem.components.OptionsSheet
import dev.aladins.focustube.core.designsystem.components.PrimaryButton
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.domain.model.Region
import java.util.Locale

/**
 * Region filter sheet (13), shared by Search (a persona's regions) and Settings (default
 * regions). Lists [selected] plus [defaults] with checkboxes; "Add a country" opens a searchable
 * list of every country. Apply needs at least one region; the order of ticking is kept.
 */
@Composable
fun RegionSheet(
    title: String,
    selected: List<Region>,
    defaults: List<Region>,
    onApply: (List<Region>) -> Unit,
    onDismiss: () -> Unit
) {
    val ticked = remember { mutableStateListOf<String>().apply { addAll(selected.map { it.code }) } }
    val shown =
        remember { mutableStateListOf<String>().apply { addAll((selected + defaults).map { it.code }.distinct()) } }
    var addingCountry by rememberSaveable { mutableStateOf(false) }
    val spacing = FocusTubeTheme.spacing

    OptionsSheet(onDismiss = onDismiss) {
        if (addingCountry) {
            CountryPicker(
                exclude = shown.toSet(),
                onPick = { code ->
                    shown.add(code)
                    ticked.add(code)
                    addingCountry = false
                }
            )
            return@OptionsSheet
        }
        Column(Modifier.navigationBarsPadding().padding(bottom = spacing.lg)) {
            Text(
                text = title,
                style = FocusTubeTheme.typography.section,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = spacing.xl).semantics { heading() }
            )
            Spacer(Modifier.padding(top = spacing.sm))
            LazyColumn(Modifier.weight(1f, fill = false)) {
                items(shown, key = { it }) { code ->
                    RegionRow(
                        code = code,
                        checked = code in ticked,
                        onToggle = { if (code in ticked) ticked.remove(code) else ticked.add(code) }
                    )
                }
            }
            Row(Modifier.padding(horizontal = spacing.sm)) {
                FtTextButton(text = "+ " + stringResource(R.string.regions_add_country), onClick = {
                    addingCountry =
                        true
                })
                Spacer(Modifier.weight(1f))
                FtTextButton(
                    text = stringResource(R.string.regions_reset),
                    onClick = {
                        ticked.clear()
                        ticked.addAll(defaults.map { it.code })
                    }
                )
            }
            if (ticked.isEmpty()) {
                Text(
                    text = stringResource(R.string.regions_need_one),
                    style = FocusTubeTheme.typography.meta,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = spacing.xl)
                )
            }
            PrimaryButton(
                text = stringResource(R.string.action_apply),
                onClick = { onApply(ticked.map(::Region)) },
                enabled = ticked.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.xl, vertical = spacing.sm)
            )
        }
    }
}

@Composable
private fun RegionRow(code: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .heightIn(min = 48.dp)
                .padding(horizontal = FocusTubeTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(FocusTubeTheme.spacing.sm))
        Text(
            text = countryName(code),
            style = FocusTubeTheme.typography.body,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = regionLabel(code),
            style = FocusTubeTheme.typography.mono,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Every country, searchable by name or code. */
@Composable
private fun CountryPicker(exclude: Set<String>, onPick: (String) -> Unit) {
    val query = rememberTextFieldState()
    val all = remember { Locale.getISOCountries().map { it to countryName(it) }.sortedBy { it.second } }
    val needle = query.text.toString().trim()
    val matches =
        all.filter { (code, name) ->
            code !in exclude &&
                (needle.isEmpty() || name.contains(needle, ignoreCase = true) || code.equals(needle, ignoreCase = true))
        }
    Column(Modifier.navigationBarsPadding()) {
        FtSearchField(
            state = query,
            onSearch = {},
            placeholder = stringResource(R.string.regions_search_country),
            clearDescription = stringResource(R.string.action_clear_search),
            modifier = Modifier.padding(horizontal = FocusTubeTheme.spacing.lg),
            leading = {
                Icon(
                    painter = painterResource(FocusTubeIcons.Globe),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
        LazyColumn(
            contentPadding = PaddingValues(vertical = FocusTubeTheme.spacing.sm),
            verticalArrangement = Arrangement.Top
        ) {
            items(matches, key = { it.first }) { (code, name) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(code) }
                        .heightIn(min = 48.dp)
                        .padding(horizontal = FocusTubeTheme.spacing.xl),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        name,
                        style = FocusTubeTheme.typography.body,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        regionLabel(code),
                        style = FocusTubeTheme.typography.mono,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
