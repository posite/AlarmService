package com.posite.my_alarm.ui.picker

import android.os.Parcelable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posite.my_alarm.ui.theme.SkyBlue
import kotlinx.parcelize.Parcelize

@Composable
fun DayOfWeekScreen(selectedDayOfWeek: MutableSet<DayOfWeek>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items(daysOfWeeks) { dayOfWeek ->
                val isSelected = remember { mutableStateOf(selectedDayOfWeek.contains(dayOfWeek)) }
                val circleRadius by animateFloatAsState(
                    targetValue = if (isSelected.value) 1f else 0f,
                    animationSpec = if (isSelected.value) {
                        tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    } else {
                        tween(durationMillis = 100)
                    },
                    label = "circle_radius"
                )
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .drawWithContent {
                            drawContent()
                            if (isSelected.value) {
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f

                                drawCircle(
                                    color = Color.Black,
                                    radius = 20.dp.toPx() * circleRadius,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                        },
                    shape = CircleShape,
                    onClick = {
                        if (isSelected.value) {
                            selectedDayOfWeek.remove(dayOfWeek)
                            isSelected.value = false
                        } else {
                            selectedDayOfWeek.add(dayOfWeek)
                            isSelected.value = true
                        }
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = dayOfWeek.label,
                            fontSize = 16.sp,
                            color = if (isSelected.value) SkyBlue else Color.Black
                        )
                    }

                }
            }
        }
    }
}

@Parcelize
enum class DayOfWeek(val value: Int, val label: String) : Parcelable {
    SUNDAY(0, "일"),
    MONDAY(1, "월"),
    TUESDAY(2, "화"),
    WEDNESDAY(3, "수"),
    THURSDAY(4, "목"),
    FRIDAY(5, "금"),
    SATURDAY(6, "토")
}

val daysOfWeeks = listOf(
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY
)