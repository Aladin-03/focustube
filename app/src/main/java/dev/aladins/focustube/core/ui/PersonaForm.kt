package dev.aladins.focustube.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.FtTextField
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.domain.model.PersonaGoalError
import dev.aladins.focustube.domain.model.PersonaLimits
import dev.aladins.focustube.domain.model.PersonaNameError

/**
 * The persona name and goal fields, shared by Welcome (01) and Create / edit persona (03).
 * Errors come from ValidatePersonaInputUseCase; this only turns them into text.
 */
@Composable
fun PersonaFormFields(
    name: String,
    goal: String,
    nameError: PersonaNameError?,
    goalError: PersonaGoalError?,
    onNameChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(FocusTubeTheme.spacing.md)) {
        FtTextField(
            value = name,
            onValueChange = onNameChange,
            label = stringResource(R.string.persona_name_label),
            placeholder = stringResource(R.string.persona_name_placeholder),
            error = nameError?.let { personaNameErrorText(it) },
            maxLength = PersonaLimits.NAME_MAX_LENGTH,
            keyboardOptions =
                KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
        )
        FtTextField(
            value = goal,
            onValueChange = onGoalChange,
            label = stringResource(R.string.persona_goal_label),
            placeholder = stringResource(R.string.persona_goal_placeholder),
            error = goalError?.let { personaGoalErrorText(it) },
            maxLength = PersonaLimits.GOAL_MAX_LENGTH,
            singleLine = false,
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
    }
}

/** User-facing text for a name error. */
@Composable
fun personaNameErrorText(error: PersonaNameError): String = when (error) {
    PersonaNameError.Empty -> stringResource(R.string.persona_error_name_empty)
    PersonaNameError.TooLong -> tooLongText(PersonaLimits.NAME_MAX_LENGTH)
    PersonaNameError.Taken -> stringResource(R.string.persona_error_name_taken)
}

/** User-facing text for a goal error. */
@Composable
fun personaGoalErrorText(error: PersonaGoalError): String = when (error) {
    PersonaGoalError.TooLong -> tooLongText(PersonaLimits.GOAL_MAX_LENGTH)
}

@PreviewLightDark
@Composable
private fun PersonaFormFieldsPreview() {
    FocusTubeTheme {
        PersonaFormFields(
            name = "Guitar",
            goal = "Learn fingerstyle guitar. I'm a beginner.",
            nameError = PersonaNameError.Taken,
            goalError = null,
            onNameChange = {},
            onGoalChange = {}
        )
    }
}

@Composable
private fun tooLongText(maxLength: Int): String =
    pluralStringResource(R.plurals.persona_error_too_long, maxLength, maxLength)
