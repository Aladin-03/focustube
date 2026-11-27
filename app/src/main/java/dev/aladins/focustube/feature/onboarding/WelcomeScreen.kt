package dev.aladins.focustube.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.FtButtonSize
import dev.aladins.focustube.core.designsystem.components.InfoBox
import dev.aladins.focustube.core.designsystem.components.PrimaryButton
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.core.ui.PersonaFormFields

/** Welcome (01): logo, "Who is this feed for?", name and goal, info box, Create persona. Stateless. */
@Composable
fun WelcomeScreen(
    state: WelcomeUiState,
    onNameChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = FocusTubeTheme.spacing
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.xl, vertical = spacing.xxl)
    ) {
        LogoMark()
        Spacer(Modifier.height(spacing.xxl))
        Text(
            text = stringResource(R.string.welcome_title),
            style = FocusTubeTheme.typography.displayTitle,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(spacing.xxl))
        PersonaFormFields(
            name = state.name,
            goal = state.goal,
            nameError = state.nameError,
            goalError = state.goalError,
            onNameChange = onNameChange,
            onGoalChange = onGoalChange
        )
        Spacer(Modifier.height(spacing.md))
        InfoBox(text = stringResource(R.string.welcome_info))
        Spacer(Modifier.height(spacing.xxl).weight(1f))
        PrimaryButton(
            text = stringResource(R.string.action_create_persona),
            onClick = onCreate,
            enabled = state.canCreate,
            size = FtButtonSize.Large,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** FocusTube's mark: the launcher symbol, white on a pine circle. */
@Composable
private fun LogoMark() {
    Box(
        modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary, FocusTubeTheme.shapes.pill),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(FocusTubeIcons.LogoMark),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(56.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun WelcomeScreenPreview() {
    FocusTubeTheme {
        WelcomeScreen(
            state = WelcomeUiState(name = "Guitar", goal = "Learn fingerstyle guitar. I'm a beginner."),
            onNameChange = {},
            onGoalChange = {},
            onCreate = {}
        )
    }
}
