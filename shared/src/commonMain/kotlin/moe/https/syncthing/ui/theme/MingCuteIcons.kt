package moe.https.syncthing.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Generated from MingCute (https://github.com/mingcute-design/mingcute-icons, Apache-2.0).
// Line icons come from core-regular with a 1.6 stroke width; solid icons come from
// core-filled. Source geometry is unchanged otherwise.
internal object MingCuteIcons

internal val MingCuteIcons.Add: ImageVector
    get() {
        if (add != null) return add!!
        add = ImageVector.Builder(
            name = "Add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.EvenOdd,
            ) {
                moveTo(12f, 3.5f)
                arcTo(1.5f, 1.5f, 0f, false, true, 13.5f, 5f)
                verticalLineToRelative(5.5f)
                horizontalLineTo(19f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 0f, 3f)
                horizontalLineToRelative(-5.5f)
                verticalLineTo(19f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -3f, 0f)
                verticalLineToRelative(-5.5f)
                horizontalLineTo(5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 0f, -3f)
                horizontalLineToRelative(5.5f)
                verticalLineTo(5f)
                arcTo(1.5f, 1.5f, 0f, false, true, 12f, 3.5f)
            }
        }.build()
        return add!!
    }

internal val MingCuteIcons.Close: ImageVector
    get() {
        if (close != null) return close!!
        close = ImageVector.Builder(
            name = "Close",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveToRelative(5.636f, 5.637f)
                lineToRelative(12.728f, 12.728f)
                moveToRelative(-12.728f, 0f)
                lineTo(18.364f, 5.637f)
            }
        }.build()
        return close!!
    }

internal val MingCuteIcons.ArrowLeft: ImageVector
    get() {
        if (arrowLeft != null) return arrowLeft!!
        arrowLeft = ImageVector.Builder(
            name = "ArrowLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(5f, 12f)
                horizontalLineToRelative(15f)
                moveToRelative(-10f, 5.657f)
                lineTo(4.343f, 12f)
                lineTo(10f, 6.343f)
            }
        }.build()
        return arrowLeft!!
    }

internal val MingCuteIcons.Check: ImageVector
    get() {
        if (check != null) return check!!
        check = ImageVector.Builder(
            name = "Check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveToRelative(3.514f, 11.83f)
                lineToRelative(5.657f, 5.656f)
                lineTo(20.485f, 6.172f)
            }
        }.build()
        return check!!
    }

internal val MingCuteIcons.Settings: ImageVector
    get() {
        if (settings != null) return settings!!
        settings = ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(10f, 2.762f)
                arcToRelative(4f, 4f, 0f, false, true, 4f, 0f)
                lineToRelative(5f, 2.887f)
                arcToRelative(4f, 4f, 0f, false, true, 2f, 3.464f)
                verticalLineToRelative(5.774f)
                arcToRelative(4f, 4f, 0f, false, true, -2f, 3.463f)
                lineToRelative(-5f, 2.887f)
                arcToRelative(4f, 4f, 0f, false, true, -4f, 0f)
                lineTo(5f, 18.35f)
                arcToRelative(4f, 4f, 0f, false, true, -2f, -3.464f)
                verticalLineTo(9.114f)
                arcTo(4f, 4f, 0f, false, true, 5f, 5.65f)
                close()
                moveTo(12f, 9f)
                arcToRelative(3f, 3f, 0f, true, false, 0f, 6f)
                arcToRelative(3f, 3f, 0f, false, false, 0f, -6f)
            }
        }.build()
        return settings!!
    }

internal val MingCuteIcons.Device: ImageVector
    get() {
        if (device != null) return device!!
        device = ImageVector.Builder(
            name = "Device",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(20f, 8f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
                verticalLineToRelative(9f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                horizontalLineToRelative(-4f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                verticalLineToRelative(-9f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                close()
                moveToRelative(-3f, -4f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
                horizontalLineToRelative(-4f)
                arcToRelative(3f, 3f, 0f, false, false, -3f, 3f)
                verticalLineToRelative(11f)
                horizontalLineTo(4f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                verticalLineTo(6f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                close()
            }
        }.build()
        return device!!
    }

internal val MingCuteIcons.Earth4: ImageVector
    get() {
        if (earth4 != null) return earth4!!
        earth4 = ImageVector.Builder(
            name = "Earth4",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(21f, 12f)
                arcToRelative(9f, 9f, 0f, false, true, -9f, 9f)
                moveToRelative(9f, -9f)
                arcToRelative(9f, 9f, 0f, false, false, -9f, -9f)
                moveToRelative(9f, 9f)
                horizontalLineTo(3f)
                moveToRelative(9f, 9f)
                arcToRelative(9f, 9f, 0f, false, true, -9f, -9f)
                moveToRelative(9f, 9f)
                curveToRelative(-1.933f, 0f, -3.5f, -4.03f, -3.5f, -9f)
                reflectiveCurveToRelative(1.567f, -9f, 3.5f, -9f)
                moveToRelative(0f, 18f)
                curveToRelative(1.933f, 0f, 3.5f, -4.03f, 3.5f, -9f)
                reflectiveCurveTo(13.933f, 3f, 12f, 3f)
                moveToRelative(-9f, 9f)
                arcToRelative(9f, 9f, 0f, false, true, 9f, -9f)
            }
        }.build()
        return earth4!!
    }

internal val MingCuteIcons.Earth4Filled: ImageVector
    get() {
        if (earth4Filled != null) return earth4Filled!!
        earth4Filled = ImageVector.Builder(
            name = "Earth4Filled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(14.98f, 13f)
                curveToRelative(-.095f, 2.533f, -.546f, 4.78f, -1.165f, 6.426f)
                curveToRelative(-.352f, .934f, -.74f, 1.626f, -1.11f, 2.065f)
                curveToRelative(-.386f, .458f, -.633f, .509f, -.705f, .509f)
                reflectiveCurveToRelative(-.319f, -.051f, -.704f, -.509f)
                curveToRelative(-.37f, -.44f, -.759f, -1.131f, -1.11f, -2.065f)
                curveTo(9.566f, 17.78f, 9.115f, 15.533f, 9.02f, 13f)
                close()
                moveToRelative(-7.96f, 0f)
                curveToRelative(.096f, 2.744f, .582f, 5.236f, 1.295f, 7.13f)
                quadToRelative(.283f, .749f, .623f, 1.39f)
                curveTo(5.217f, 20.324f, 2.448f, 17.012f, 2.05f, 13f)
                close()
                moveToRelative(14.931f, 0f)
                curveToRelative(-.398f, 4.012f, -3.168f, 7.324f, -6.887f, 8.52f)
                quadToRelative(.34f, -.64f, .622f, -1.39f)
                curveToRelative(.714f, -1.893f, 1.2f, -4.386f, 1.296f, -7.13f)
                close()
                moveTo(8.937f, 2.48f)
                curveToRelative(-.227f, .425f, -.435f, .891f, -.623f, 1.39f)
                curveToRelative(-.713f, 1.893f, -1.2f, 4.386f, -1.295f, 7.13f)
                horizontalLineToRelative(-4.97f)
                arcToRelative(10.01f, 10.01f, 0f, false, true, 6.888f, -8.52f)
                moveTo(12f, 2f)
                curveToRelative(.072f, 0f, .319f, .051f, .704f, .509f)
                curveToRelative(.37f, .44f, .759f, 1.132f, 1.11f, 2.065f)
                curveToRelative(.62f, 1.646f, 1.071f, 3.893f, 1.166f, 6.426f)
                horizontalLineTo(9.02f)
                curveToRelative(.095f, -2.533f, .546f, -4.78f, 1.165f, -6.426f)
                curveToRelative(.352f, -.933f, .74f, -1.626f, 1.11f, -2.065f)
                curveTo(11.682f, 2.05f, 11.929f, 2f, 12f, 2f)
                moveToRelative(3.063f, .48f)
                arcTo(10f, 10f, 0f, false, true, 21.95f, 11f)
                horizontalLineToRelative(-4.969f)
                curveToRelative(-.096f, -2.744f, -.582f, -5.237f, -1.296f, -7.13f)
                arcToRelative(12f, 12f, 0f, false, false, -.623f, -1.39f)
            }
        }.build()
        return earth4Filled!!
    }

internal val MingCuteIcons.Link3: ImageVector
    get() {
        if (link3 != null) return link3!!
        link3 = ImageVector.Builder(
            name = "Link3",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(10.626f, 8.673f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, .748f, 2.906f)
                arcTo(2.501f, 2.501f, 0f, false, false, 12f, 16.5f)
                horizontalLineToRelative(6f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, 1.249f, -4.666f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 1.502f, -2.598f)
                arcTo(5.5f, 5.5f, 0f, false, true, 18f, 19.5f)
                horizontalLineToRelative(-6f)
                arcToRelative(5.5f, 5.5f, 0f, false, true, -1.374f, -10.827f)
                moveTo(12f, 4.5f)
                arcToRelative(5.5f, 5.5f, 0f, false, true, 1.374f, 10.826f)
                arcToRelative(1.5f, 1.5f, 0f, true, true, -.748f, -2.905f)
                arcTo(2.501f, 2.501f, 0f, false, false, 12f, 7.5f)
                horizontalLineTo(6f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, -1.249f, 4.666f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.502f, 2.598f)
                arcTo(5.5f, 5.5f, 0f, false, true, 6f, 4.5f)
                close()
            }
        }.build()
        return link3!!
    }

internal val MingCuteIcons.Lock: ImageVector
    get() {
        if (lock != null) return lock!!
        lock = ImageVector.Builder(
            name = "Lock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 2f)
                arcToRelative(6f, 6f, 0f, false, true, 6f, 6f)
                horizontalLineToRelative(1f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
                verticalLineToRelative(10f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                horizontalLineTo(5f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                verticalLineTo(10f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                horizontalLineToRelative(1f)
                arcToRelative(6f, 6f, 0f, false, true, 6f, -6f)
                moveToRelative(-.107f, 10.005f)
                arcTo(1.998f, 1.998f, 0f, false, false, 11f, 15.729f)
                verticalLineTo(17f)
                arcToRelative(1f, 1f, 0f, true, false, 2f, 0f)
                verticalLineToRelative(-1.27f)
                arcToRelative(1.997f, 1.997f, 0f, false, false, -.894f, -3.725f)
                arcToRelative(1f, 1f, 0f, false, false, -.213f, 0f)
                moveTo(12f, 4f)
                arcToRelative(4f, 4f, 0f, false, false, -4f, 4f)
                horizontalLineToRelative(8f)
                arcToRelative(4f, 4f, 0f, false, false, -4f, -4f)
            }
        }.build()
        return lock!!
    }

internal val MingCuteIcons.Info: ImageVector
    get() {
        if (info != null) return info!!
        info = ImageVector.Builder(
            name = "Info",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 2f)
                curveToRelative(5.523f, 0f, 10f, 4.477f, 10f, 10f)
                reflectiveCurveToRelative(-4.477f, 10f, -10f, 10f)
                reflectiveCurveTo(2f, 17.523f, 2f, 12f)
                reflectiveCurveTo(6.477f, 2f, 12f, 2f)
                moveToRelative(-1f, 8f)
                arcToRelative(1f, 1f, 0f, true, false, 0f, 2f)
                verticalLineToRelative(5f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                horizontalLineToRelative(.5f)
                arcToRelative(1f, 1f, 0f, false, false, .5f, -1.865f)
                verticalLineTo(11f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                close()
                moveToRelative(1f, -3f)
                arcToRelative(1f, 1f, 0f, true, false, 0f, 2f)
                horizontalLineToRelative(.002f)
                arcToRelative(1f, 1f, 0f, false, false, 0f, -2f)
                close()
            }
        }.build()
        return info!!
    }

internal val MingCuteIcons.Refresh: ImageVector
    get() {
        if (refresh != null) return refresh!!
        refresh = ImageVector.Builder(
            name = "Refresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveToRelative(19.96f, 10.41f)
                lineToRelative(-1.708f, -.382f)
                curveToRelative(-.107f, -.024f, -.169f, .12f, -.077f, .181f)
                lineToRelative(2.673f, 1.801f)
                arcToRelative(.098f, .098f, 0f, false, false, .154f, -.081f)
                arcTo(9.003f, 9.003f, 0f, false, false, 3.307f, 9.67f)
                arcTo(9f, 9f, 0f, false, false, 19.487f, 17f)
            }
        }.build()
        return refresh!!
    }

internal val MingCuteIcons.Home4: ImageVector
    get() {
        if (home4 != null) return home4!!
        home4 = ImageVector.Builder(
            name = "Home4",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(13.2f, 2.65f)
                arcToRelative(2f, 2f, 0f, false, false, -2.4f, 0f)
                lineToRelative(-7f, 5.25f)
                arcTo(2f, 2f, 0f, false, false, 3f, 9.5f)
                verticalLineTo(19f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, 2f)
                horizontalLineToRelative(3.9f)
                arcToRelative(1.1f, 1.1f, 0f, false, false, 1.1f, -1.1f)
                verticalLineTo(15f)
                arcToRelative(2f, 2f, 0f, true, true, 4f, 0f)
                verticalLineToRelative(4.9f)
                arcToRelative(1.1f, 1.1f, 0f, false, false, 1.1f, 1.1f)
                horizontalLineTo(19f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, -2f)
                verticalLineTo(9.5f)
                arcToRelative(2f, 2f, 0f, false, false, -.8f, -1.6f)
                close()
            }
        }.build()
        return home4!!
    }

internal val MingCuteIcons.Help: ImageVector
    get() {
        if (help != null) return help!!
        help = ImageVector.Builder(
            name = "Help",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 17f)
                horizontalLineToRelative(.002f)
                moveToRelative(-2.627f, -6.875f)
                arcToRelative(2.625f, 2.625f, 0f, true, true, 3.601f, 2.438f)
                curveToRelative(-.512f, .205f, -.976f, .635f, -.976f, 1.187f)
                verticalLineTo(14f)
                moveToRelative(9f, -2f)
                arcToRelative(9f, 9f, 0f, true, true, -18f, 0f)
                arcToRelative(9f, 9f, 0f, false, true, 18f, 0f)
            }
        }.build()
        return help!!
    }

internal val MingCuteIcons.Folder: ImageVector
    get() {
        if (folder != null) return folder!!
        folder = ImageVector.Builder(
            name = "Folder",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(2f, 5f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                horizontalLineToRelative(5.52f)
                arcToRelative(2f, 2f, 0f, false, true, 1.561f, .75f)
                lineToRelative(1.4f, 1.75f)
                horizontalLineTo(20f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
                verticalLineTo(19f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                horizontalLineTo(4f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                close()
            }
        }.build()
        return folder!!
    }

internal val MingCuteIcons.File: ImageVector
    get() {
        if (file != null) return file!!
        file = ImageVector.Builder(
            name = "File",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(13f, 3f)
                verticalLineToRelative(5.5f)
                arcToRelative(.5f, .5f, 0f, false, false, .5f, .5f)
                horizontalLineTo(19f)
                moveToRelative(-5.414f, -6f)
                horizontalLineTo(6f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                verticalLineToRelative(16f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                horizontalLineToRelative(12f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, -1f)
                verticalLineTo(8.414f)
                arcToRelative(1f, 1f, 0f, false, false, -.293f, -.707f)
                lineToRelative(-4.414f, -4.414f)
                arcTo(1f, 1f, 0f, false, false, 13.586f, 3f)
                close()
            }
        }.build()
        return file!!
    }

internal val MingCuteIcons.FileDownload: ImageVector
    get() {
        if (fileDownload != null) return fileDownload!!
        fileDownload = ImageVector.Builder(
            name = "FileDownload",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 8.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, false, 1.5f, 1.5f)
                horizontalLineTo(20f)
                verticalLineToRelative(10f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                horizontalLineTo(6f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                verticalLineTo(4f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                horizontalLineToRelative(6f)
                close()
                moveToRelative(0f, 3.5f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                verticalLineToRelative(2.708f)
                lineToRelative(-.414f, -.414f)
                arcToRelative(1f, 1f, 0f, false, false, -1.414f, 1.414f)
                lineToRelative(2.121f, 2.122f)
                arcToRelative(1f, 1f, 0f, false, false, 1.414f, 0f)
                lineToRelative(2.122f, -2.122f)
                arcToRelative(1f, 1f, 0f, false, false, -1.415f, -1.414f)
                lineToRelative(-.414f, .414f)
                verticalLineTo(13f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                moveToRelative(2f, -9.956f)
                arcToRelative(2f, 2f, 0f, false, true, 1f, .542f)
                lineTo(19.414f, 7f)
                arcToRelative(2f, 2f, 0f, false, true, .541f, 1f)
                horizontalLineTo(14f)
                close()
            }
        }.build()
        return fileDownload!!
    }

internal val MingCuteIcons.ChevronRight: ImageVector
    get() {
        if (chevronRight != null) return chevronRight!!
        chevronRight = ImageVector.Builder(
            name = "ChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(9.343f, 6.343f)
                lineTo(15f, 12f)
                lineToRelative(-5.657f, 5.657f)
            }
        }.build()
        return chevronRight!!
    }

internal val MingCuteIcons.UploadCloud: ImageVector
    get() {
        if (uploadCloud != null) return uploadCloud!!
        uploadCloud = ImageVector.Builder(
            name = "UploadCloud",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(8f, 9f)
                horizontalLineTo(6f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                verticalLineToRelative(10f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                horizontalLineToRelative(12f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, -1f)
                verticalLineTo(10f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                horizontalLineToRelative(-2f)
                moveToRelative(-4f, -6f)
                verticalLineToRelative(11f)
                moveToRelative(2.5f, -8.5f)
                lineTo(12f, 3f)
                lineTo(9.5f, 5.5f)
            }
        }.build()
        return uploadCloud!!
    }

internal val MingCuteIcons.Theme: ImageVector
    get() {
        if (theme != null) return theme!!
        theme = ImageVector.Builder(
            name = "Theme",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 2f)
                curveToRelative(5.523f, 0f, 10f, 4.477f, 10f, 10f)
                quadToRelative(-.002f, .975f, -.18f, 1.9f)
                curveToRelative(-.386f, 2.003f, -2.398f, 2.85f, -4.083f, 2.57f)
                lineToRelative(-1.739f, -.29f)
                arcToRelative(1.29f, 1.29f, 0f, false, false, -1.124f, .36f)
                lineToRelative(-.089f, .089f)
                curveToRelative(-.34f, .34f, -.423f, .858f, -.209f, 1.287f)
                curveToRelative(.423f, .846f, .428f, 1.812f, .055f, 2.604f)
                arcToRelative(2.53f, 2.53f, 0f, false, true, -2.222f, 1.472f)
                quadToRelative(-.204f, .009f, -.409f, .008f)
                curveTo(6.477f, 22f, 2f, 17.523f, 2f, 12f)
                reflectiveCurveTo(6.477f, 2f, 12f, 2f)
                moveToRelative(-4.5f, 9f)
                arcToRelative(1.5f, 1.5f, 0f, true, false, 0f, 3f)
                arcToRelative(1.5f, 1.5f, 0f, false, false, 0f, -3f)
                moveToRelative(2f, -4f)
                arcToRelative(1.5f, 1.5f, 0f, true, false, 0f, 3f)
                arcToRelative(1.5f, 1.5f, 0f, false, false, 0f, -3f)
                moveToRelative(5f, 0f)
                arcToRelative(1.5f, 1.5f, 0f, true, false, 0f, 3f)
                arcToRelative(1.5f, 1.5f, 0f, false, false, 0f, -3f)
            }
        }.build()
        return theme!!
    }

internal val MingCuteIcons.Backup: ImageVector
    get() {
        if (backup != null) return backup!!
        backup = ImageVector.Builder(
            name = "Backup",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(21f, 19f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                horizontalLineTo(5f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                verticalLineTo(9f)
                horizontalLineToRelative(18f)
                close()
                moveToRelative(-9f, -8f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                verticalLineToRelative(3.185f)
                lineToRelative(-.414f, -.414f)
                arcToRelative(1f, 1f, 0f, false, false, -1.414f, 1.415f)
                lineToRelative(2.121f, 2.12f)
                arcToRelative(1f, 1f, 0f, false, false, 1.414f, 0f)
                lineToRelative(2.121f, -2.12f)
                arcToRelative(1f, 1f, 0f, true, false, -1.414f, -1.415f)
                lineToRelative(-.414f, .414f)
                verticalLineTo(12f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                moveToRelative(4.586f, -8f)
                arcTo(2f, 2f, 0f, false, true, 18f, 3.586f)
                lineTo(20.414f, 6f)
                arcToRelative(2f, 2f, 0f, false, true, .541f, 1f)
                horizontalLineTo(3.045f)
                curveToRelative(.08f, -.376f, .265f, -.724f, .54f, -1f)
                lineTo(6f, 3.586f)
                arcTo(2f, 2f, 0f, false, true, 7.414f, 3f)
                close()
            }
        }.build()
        return backup!!
    }

internal val MingCuteIcons.Database: ImageVector
    get() {
        if (database != null) return database!!
        database = ImageVector.Builder(
            name = "Database",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(8f, 17f)
                horizontalLineToRelative(2f)
                moveTo(8f, 8f)
                horizontalLineToRelative(2f)
                moveTo(5f, 20f)
                horizontalLineToRelative(14f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, -1f)
                verticalLineToRelative(-4f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                horizontalLineTo(5f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                verticalLineToRelative(4f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                close()
                moveToRelative(0f, -9f)
                horizontalLineToRelative(14f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, -1f)
                verticalLineTo(6f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                horizontalLineTo(5f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                verticalLineToRelative(4f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                close()
            }
        }.build()
        return database!!
    }

internal val MingCuteIcons.Tool: ImageVector
    get() {
        if (tool != null) return tool!!
        tool = ImageVector.Builder(
            name = "Tool",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(4.338f, 6.764f)
                curveToRelative(-.435f, -.527f, -1.259f, -.466f, -1.497f, .174f)
                arcToRelative(7.001f, 7.001f, 0f, false, false, 8.299f, 9.227f)
                curveToRelative(.423f, -.108f, .882f, .016f, 1.15f, .36f)
                lineToRelative(2.966f, 3.825f)
                arcToRelative(3.648f, 3.648f, 0f, true, false, 5.126f, -5.113f)
                lineToRelative(-3.834f, -2.99f)
                curveToRelative(-.342f, -.267f, -.466f, -.724f, -.36f, -1.145f)
                arcToRelative(7.001f, 7.001f, 0f, false, false, -9.202f, -8.291f)
                curveToRelative(-.642f, .235f, -.706f, 1.06f, -.18f, 1.497f)
                lineToRelative(3.732f, 3.103f)
                arcToRelative(1f, 1f, 0f, false, true, .341f, .965f)
                lineToRelative(-.282f, 1.416f)
                arcToRelative(1f, 1f, 0f, false, true, -.786f, .785f)
                lineToRelative(-1.411f, .28f)
                arcToRelative(1f, 1f, 0f, false, true, -.966f, -.344f)
                close()
            }
        }.build()
        return tool!!
    }

internal val MingCuteIcons.Globe: ImageVector
    get() {
        if (globe != null) return globe!!
        globe = ImageVector.Builder(
            name = "Globe",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 18.001f)
                arcToRelative(9f, 9f, 0f, false, false, 6f, -15.708f)
                moveToRelative(-6f, 15.708f)
                arcToRelative(8.99f, 8.99f, 0f, false, true, -7.5f, -4.023f)
                moveToRelative(7.5f, 4.023f)
                verticalLineToRelative(3f)
                moveToRelative(-4f, 0f)
                horizontalLineToRelative(8f)
                moveToRelative(2f, -12f)
                arcToRelative(6f, 6f, 0f, true, true, -12f, 0f)
                arcToRelative(6f, 6f, 0f, false, true, 12f, 0f)
                close()
            }
        }.build()
        return globe!!
    }

internal val MingCuteIcons.Scan: ImageVector
    get() {
        if (scan != null) return scan!!
        scan = ImageVector.Builder(
            name = "Scan",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(4f, 8f)
                verticalLineTo(5f)
                arcToRelative(1f, 1f, 0f, false, true, 1f, -1f)
                horizontalLineToRelative(4f)
                moveTo(4f, 16f)
                verticalLineToRelative(3f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                horizontalLineToRelative(4f)
                moveTo(20f, 8f)
                verticalLineTo(5f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                horizontalLineToRelative(-4f)
                moveToRelative(5f, 12f)
                verticalLineToRelative(3f)
                arcToRelative(1f, 1f, 0f, false, true, -1f, 1f)
                horizontalLineToRelative(-4f)
                moveTo(4f, 12f)
                horizontalLineToRelative(16f)
            }
        }.build()
        return scan!!
    }

private var add: ImageVector? = null
private var close: ImageVector? = null
private var arrowLeft: ImageVector? = null
private var check: ImageVector? = null
private var settings: ImageVector? = null
private var device: ImageVector? = null
private var earth4: ImageVector? = null
private var earth4Filled: ImageVector? = null
private var link3: ImageVector? = null
private var lock: ImageVector? = null
private var info: ImageVector? = null
private var refresh: ImageVector? = null
private var home4: ImageVector? = null
private var help: ImageVector? = null
private var folder: ImageVector? = null
private var file: ImageVector? = null
private var fileDownload: ImageVector? = null
private var chevronRight: ImageVector? = null
private var uploadCloud: ImageVector? = null
private var theme: ImageVector? = null
private var backup: ImageVector? = null
private var database: ImageVector? = null
private var tool: ImageVector? = null
private var globe: ImageVector? = null
private var scan: ImageVector? = null
