package com.posite.my_alarm.ui.slide

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun CircleUnlock(onSwipe: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomCenter)
                .background(shape = CircleShape, color = Color(0xFFDDDDDD))
                .clipToBounds(),
        ) {
            val density = LocalDensity.current
            val coroutineScope = rememberCoroutineScope()

            val parentWidthPx = constraints.maxWidth
            val parentHeightPx = constraints.maxHeight

            val circleDiameter = 70.dp
            val circleDiameterPx = density.run { circleDiameter.toPx() }

            val startX = density.run { 65.dp.toPx() }
            val startY = density.run { 65.dp.toPx() }

            val limit = density.run { 65.dp.toPx() }
            var isDragging by remember { mutableStateOf(false) }
            val offsetX = remember { Animatable(startX) }
            val offsetY = remember { Animatable(startY) }

            LaunchedEffect(offsetX.value, offsetY.value) {
                if((startX - offsetX.value).pow(2) + (startY - offsetY.value).pow(2) >= limit.pow(2)) {
                    onSwipe()
                }
            }

            // 드래그가 끝났을 때 위치 확인 및 원래 위치로 돌아가기
            LaunchedEffect(isDragging) {
                if (!isDragging) {
                    val distanceSquared =
                        (startX - offsetX.value).pow(2) + (startY - offsetY.value).pow(2)
                    if (distanceSquared < limit.pow(2)) {
                        // 일정 거리 미만이면 원래 위치로 애니메이션
                        launch { offsetX.animateTo(startX, spring()) }
                        launch { offsetY.animateTo(startY, spring()) }
                    } else {
                        onSwipe()
                    }
                }
            }

            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .size(circleDiameter)
                    .offset {
                        IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt())
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val newX = (offsetX.value + dragAmount.x)
                                    .coerceIn(0f, parentWidthPx - circleDiameterPx)
                                val newY = (offsetY.value + dragAmount.y)
                                    .coerceIn(0f, parentHeightPx - circleDiameterPx)

                                // 코루틴 범위 내에서 값 업데이트
                                coroutineScope.launch {
                                    offsetX.snapTo(newX)
                                    offsetY.snapTo(newY)
                                }
                            }
                        )
                    }
                    .clipToBounds(),
                color = Color(0xFFEEEEEE),
            ) {
                // Surface 내용
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircleUnlockPreview() {
    CircleUnlock()
}