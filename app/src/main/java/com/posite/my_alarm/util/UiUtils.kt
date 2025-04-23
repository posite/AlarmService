package com.posite.my_alarm.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.roundedRippleClickable(
    dp: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val radius = dpToFloat(dp) * 3f
    val isPressed = interactionSource.collectIsPressedAsState()

    this
        .drawBehind {
            if (isPressed.value) {
                drawRoundRect(
                    cornerRadius = CornerRadius(radius),
                    color = Color.Gray.copy(alpha = 0.1f),
                    size = size
                )
            }
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null, // 기본 indication은 사용하지 않고 직접 그림
            onLongClick = onLongClick,
            onClick = onClick
        )
}

@Composable
fun dpToFloat(dp: Dp) = with(LocalDensity.current) { dp.value }

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }