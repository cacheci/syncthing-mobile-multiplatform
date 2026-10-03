package moe.https.syncthing.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal object LocalFeatherIcons

internal val LocalFeatherIcons.Database: ImageVector
    get() {
        if (database != null) return database!!
        database = ImageVector.Builder(
            name = "Database",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            featherPath {
                moveTo(3.0f, 5.0f)
                arcToRelative(9.0f, 3.0f, 0.0f, true, false, 18.0f, 0.0f)
                arcToRelative(9.0f, 3.0f, 0.0f, true, false, -18.0f, 0.0f)
                close()
            }
            featherPath {
                moveTo(21.0f, 12.0f)
                curveToRelative(0.0f, 1.66f, -4.0f, 3.0f, -9.0f, 3.0f)
                reflectiveCurveToRelative(-9.0f, -1.34f, -9.0f, -3.0f)
            }
            featherPath {
                moveTo(3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.66f, 4.0f, 3.0f, 9.0f, 3.0f)
                reflectiveCurveToRelative(9.0f, -1.34f, 9.0f, -3.0f)
                verticalLineTo(5.0f)
            }
        }.build()
        return database!!
    }

internal val LocalFeatherIcons.Tool: ImageVector
    get() {
        if (tool != null) return tool!!
        tool = ImageVector.Builder(
            name = "Tool",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            featherPath {
                moveTo(14.7f, 6.3f)
                arcToRelative(1f, 1f, 0f, false, false, 0f, 1.4f)
                lineToRelative(1.6f, 1.6f)
                arcToRelative(1f, 1f, 0f, false, false, 1.4f, 0f)
                lineToRelative(3.77f, -3.77f)
                arcToRelative(6f, 6f, 0f, false, true, -7.94f, 7.94f)
                lineToRelative(-6.91f, 6.91f)
                arcToRelative(2.12f, 2.12f, 0f, false, true, -3f, -3f)
                lineToRelative(6.91f, -6.91f)
                arcToRelative(6f, 6f, 0f, false, true, 7.94f, -7.94f)
                lineToRelative(-3.76f, 3.76f)
                close()
            }
        }.build()
        return tool!!
    }

internal val LocalFeatherIcons.Globe: ImageVector
    get() {
        if (globe != null) return globe!!
        globe = ImageVector.Builder(
            name = "Globe",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            featherPath {
                moveTo(12.0f, 12.0f)
                moveToRelative(-10.0f, 0.0f)
                arcToRelative(10.0f, 10.0f, 0.0f, true, true, 20.0f, 0.0f)
                arcToRelative(10.0f, 10.0f, 0.0f, true, true, -20.0f, 0.0f)
            }
            featherPath {
                moveTo(2.0f, 12.0f)
                lineTo(22.0f, 12.0f)
            }
            featherPath {
                moveTo(12.0f, 2.0f)
                arcToRelative(15.3f, 15.3f, 0.0f, false, true, 4.0f, 10.0f)
                arcToRelative(15.3f, 15.3f, 0.0f, false, true, -4.0f, 10.0f)
                arcToRelative(15.3f, 15.3f, 0.0f, false, true, -4.0f, -10.0f)
                arcToRelative(15.3f, 15.3f, 0.0f, false, true, 4.0f, -10.0f)
                close()
            }
        }.build()
        return globe!!
    }

private fun ImageVector.Builder.featherPath(pathBuilder: PathBuilder.() -> Unit) {
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.6f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineMiter = 4f,
        pathFillType = PathFillType.NonZero,
        pathBuilder = pathBuilder,
    )
}

private var database: ImageVector? = null
private var tool: ImageVector? = null
private var globe: ImageVector? = null
