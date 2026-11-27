package dev.aladins.focustube.debug

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/** Debug builds only: an empty Hilt activity that UI tests put composables in. */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
