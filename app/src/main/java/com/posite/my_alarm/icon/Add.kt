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

public val Add: ImageVector
    get() {
        if (_Add != null) {
            return _Add!!
        }
        _Add = ImageVector.Builder(
            name = "Add",
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
                    moveTo(490.667f, 234.667f)
                    horizontalLineTo(277.333f)
                    verticalLineTo(21.333f)
                    curveTo(277.3330f, 9.5510f, 267.7820f, 00f, 256f, 00f)
                    curveToRelative(-11.7820f, 00f, -21.3330f, 9.5510f, -21.3330f, 21.3330f)
                    verticalLineToRelative(213.333f)
                    horizontalLineTo(21.333f)
                    curveTo(9.551f, 234.667f, 00f, 244.2180f, 00f, 256f)
                    curveToRelative(00f, 11.7820f, 9.5510f, 21.3330f, 21.3330f, 21.3330f)
                    horizontalLineToRelative(213.333f)
                    verticalLineToRelative(213.333f)
                    curveToRelative(00f, 11.7820f, 9.5510f, 21.3330f, 21.3330f, 21.3330f)
                    curveToRelative(11.7820f, 00f, 21.3330f, -9.5510f, 21.3330f, -21.3330f)
                    verticalLineTo(277.333f)
                    horizontalLineToRelative(213.333f)
                    curveToRelative(11.7820f, 00f, 21.3330f, -9.5510f, 21.3330f, -21.3330f)
                    curveTo(512f, 244.2180f, 502.4490f, 234.6670f, 490.6670f, 234.6670f)
                    close()
                }
            }
        }.build()
        return _Add!!
    }

private var _Add: ImageVector? = null


@Preview(showBackground = true)
@Composable
fun AddPreview() {
    Icon(
        imageVector = Add,
        contentDescription = "Add Icon",
        tint = Color.Black
    )
}

