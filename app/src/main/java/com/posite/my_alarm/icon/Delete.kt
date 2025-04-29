package com.posite.my_alarm.icon

/*
* Converted using https://composables.com/svgtocompose
* https://www.flaticon.com/uicons의 UIcon
*/

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

public val Delete: ImageVector
    get() {
        if (_Delete != null) {
            return _Delete!!
        }
        _Delete = ImageVector.Builder(
            name = "Delete_icon",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            group {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(448f, 85.333f)
                    horizontalLineToRelative(-66.133f)
                    curveTo(371.660f, 35.7030f, 328.0020f, 0.0640f, 277.3330f, 00f)
                    horizontalLineToRelative(-42.667f)
                    curveToRelative(-50.6690f, 0.0640f, -94.3270f, 35.7030f, -104.5330f, 85.3330f)
                    horizontalLineTo(64f)
                    curveToRelative(-11.7820f, 00f, -21.3330f, 9.5510f, -21.3330f, 21.3330f)
                    reflectiveCurveTo(52.218f, 128f, 64f, 128f)
                    horizontalLineToRelative(21.333f)
                    verticalLineToRelative(277.333f)
                    curveTo(85.4040f, 464.2140f, 133.1190f, 511.930f, 192f, 512f)
                    horizontalLineToRelative(128f)
                    curveToRelative(58.8810f, -0.070f, 106.5960f, -47.7860f, 106.6670f, -106.6670f)
                    verticalLineTo(128f)
                    horizontalLineTo(448f)
                    curveToRelative(11.7820f, 00f, 21.3330f, -9.5510f, 21.3330f, -21.3330f)
                    reflectiveCurveTo(459.782f, 85.333f, 448f, 85.333f)
                    close()
                    moveTo(234.667f, 362.667f)
                    curveToRelative(00f, 11.7820f, -9.5510f, 21.3330f, -21.3330f, 21.3330f)
                    curveTo(201.5510f, 384f, 192f, 374.4490f, 192f, 362.6670f)
                    verticalLineToRelative(-128f)
                    curveToRelative(00f, -11.7820f, 9.5510f, -21.3330f, 21.3330f, -21.3330f)
                    curveToRelative(11.7820f, 00f, 21.3330f, 9.5510f, 21.3330f, 21.3330f)
                    verticalLineTo(362.667f)
                    close()
                    moveTo(320f, 362.667f)
                    curveToRelative(00f, 11.7820f, -9.5510f, 21.3330f, -21.3330f, 21.3330f)
                    curveToRelative(-11.7820f, 00f, -21.3330f, -9.5510f, -21.3330f, -21.3330f)
                    verticalLineToRelative(-128f)
                    curveToRelative(00f, -11.7820f, 9.5510f, -21.3330f, 21.3330f, -21.3330f)
                    curveToRelative(11.7820f, 00f, 21.3330f, 9.5510f, 21.3330f, 21.3330f)
                    verticalLineTo(362.667f)
                    close()
                    moveTo(174.315f, 85.333f)
                    curveToRelative(9.0740f, -25.5510f, 33.2380f, -42.6340f, 60.3520f, -42.6670f)
                    horizontalLineToRelative(42.667f)
                    curveToRelative(27.1140f, 0.0330f, 51.2780f, 17.1160f, 60.3520f, 42.6670f)
                    horizontalLineTo(174.315f)
                    close()
                }
            }
        }.build()
        return _Delete!!
    }

private var _Delete: ImageVector? = null

@Preview(showBackground = true)
@Composable
fun DeletePreview() {
    Icon(
        imageVector = Delete,
        contentDescription = "Add Icon",
        tint = Color.Black
    )
}

