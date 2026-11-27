package dev.aladins.focustube.core.designsystem.icons

import androidx.annotation.DrawableRes
import dev.aladins.focustube.R

/**
 * Every icon FocusTube uses, in one place. Material Symbols Rounded (outlined), stored as
 * vector drawables. Use with `painterResource(FocusTubeIcons.Search)`.
 */
object FocusTubeIcons {
    @DrawableRes val Picks = R.drawable.ic_auto_awesome

    @DrawableRes val Channels = R.drawable.ic_smart_display

    @DrawableRes val Search = R.drawable.ic_search

    @DrawableRes val Library = R.drawable.ic_bookmark

    @DrawableRes val Settings = R.drawable.ic_settings

    @DrawableRes val Back = R.drawable.ic_arrow_back

    @DrawableRes val ChevronDown = R.drawable.ic_expand_more

    @DrawableRes val Add = R.drawable.ic_add

    @DrawableRes val Globe = R.drawable.ic_language

    @DrawableRes val Edit = R.drawable.ic_edit

    @DrawableRes val Info = R.drawable.ic_info

    /** FocusTube's mark (the launcher icon's foreground: white, on a 108 dp canvas). */
    @DrawableRes val LogoMark = R.drawable.ic_launcher_foreground
}
