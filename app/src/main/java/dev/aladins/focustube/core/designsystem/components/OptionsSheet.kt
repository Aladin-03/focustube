package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** FocusTube's bottom sheet: surface colour, 18 dp top corners, opens fully. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsSheet(onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(SheetValue.Hidden, setOf(SheetValue.Hidden, SheetValue.Expanded)),
        shape = FocusTubeTheme.shapes.sheet,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
        content = content
    )
}
