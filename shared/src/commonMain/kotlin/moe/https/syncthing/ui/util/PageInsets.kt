package moe.https.syncthing.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Extra room kept below the content of a page, on top of the system navigation bar inset.
 *
 * The inset alone only prevents content from being covered by the gesture / three-button
 * navigation bar; without this extra space the last row ends flush against it.
 */
internal val PageBottomSpacing: Dp = 12.dp

/**
 * Returns [this] with [PageBottomSpacing] added below the existing bottom inset.
 *
 * Pass the result to a page's content root instead of the raw Scaffold padding so that every
 * page keeps a visible gap between its scrollable content and the navigation bar.
 */
@Composable
internal fun PaddingValues.withPageBottomSpacing(): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding(),
        end = calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + PageBottomSpacing,
    )
}
