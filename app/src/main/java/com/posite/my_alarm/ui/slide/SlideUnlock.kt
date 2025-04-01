package com.posite.my_alarm.ui.slide

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.posite.my_alarm.util.SlidePosition
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeUnlockButton(
    modifier: Modifier = Modifier,
) {
    var componentSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    val density = LocalDensity.current
    val state = remember {
        AnchoredDraggableState(
            initialValue = SlidePosition.Start,
            positionalThreshold = { totalDistance: Float -> totalDistance * 0.5f },
            velocityThreshold = { Float.MAX_VALUE },
            animationSpec = tween(),
        )
    }
    LaunchedEffect(componentSize) {
        if (componentSize.width > 0) {
            val endPosition = with(density) { (componentSize.width - 60.dp.toPx()) }
            state.updateAnchors(
                DraggableAnchors {
                    SlidePosition.Start at -0f
                    SlidePosition.End at endPosition
                }
            )
        }
    }
    var swipeText by remember {
        mutableStateOf("밀어서 잠금 해제")
    }

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SlidePosition.Start && state.offset > componentSize.width.toFloat() / 2) {
            state.snapTo(SlidePosition.End)
        }

        if (state.offset > componentSize.width.toFloat() / 2) {
            swipeText = "잠금 해제 완료"
        } else {
            swipeText = "밀어서 잠금 해제"
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp, 8.dp, 8.dp, 48.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFD3D3D3), Color(0xFFB0B0B0))
                    ), shape = RoundedCornerShape(45.dp)
                )
                .align(Alignment.BottomStart)
                .onGloballyPositioned {
                    componentSize = it.size
                }
        ) {
            Text(
                text = swipeText,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        val safeOffset = if (state.offset.isNaN()) -0f else state.offset

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset {
                    IntOffset((safeOffset.roundToInt()), 0)
                }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .size(60.dp)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color.White) // 원형 표시기 배경색
                    .anchoredDraggable(
                        state = state,
                        orientation = Orientation.Horizontal
                    )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.Gray, // 색상 변경
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SwipeUnlockButtonPreview() {
    SwipeUnlockButton()
}