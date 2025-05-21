package com.posite.my_alarm.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posite.my_alarm.R
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.util.roundedRippleClickable

@Composable
fun Alarm(
    modifier: Modifier = Modifier,
    alarm: AlarmStateEntity,
    isDeleteMode: MutableState<Boolean>,
    isSelected: Boolean,
    onAlarmSelected: () -> Unit,
    onAlarmUnselected: () -> Unit,
    onSwitchChanges: (Boolean) -> Unit,
    onAlarmClicked: () -> Unit
) {
    val isChecked = remember { mutableStateOf(alarm.isActive) }
    //Log.d("dp", "30dp: ${30.dp.value}")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(15.dp), color = Color(0xFFEEEEEE))
            .height(120.dp)
            .roundedRippleClickable(15.dp, onClick = {
                if (isDeleteMode.value) {
                    if (isSelected) {
                        onAlarmUnselected()
                    } else {
                        onAlarmSelected()
                    }
                } else {
                    onAlarmClicked()
                }
            }, onLongClick = {
                isDeleteMode.value = true
                onAlarmSelected()
            })
            .padding(4.dp, 0.dp, 16.dp, 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val textColor = if (isChecked.value) Color.Black else Color.Gray
        Row(modifier = Modifier.height(40.dp), verticalAlignment = Alignment.Bottom) {
            if (isDeleteMode.value) {
                RadioButton(
                    modifier = Modifier.height(30.dp),
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            onAlarmUnselected()
                        } else {
                            onAlarmSelected()
                        }
                    })
            }
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(0.dp),
                color = Color.Transparent
            )
            Text(text = alarm.meridiem, fontSize = 16.sp, color = textColor)
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .height(0.dp),
                color = Color.Transparent
            )
            Text(
                text = stringResource(
                    R.string.alarm_time,
                    if (alarm.meridiem == stringResource(R.string.am) && alarm.hour == 0) 12 else alarm.hour,
                    alarm.minute
                ),
                fontSize = 32.sp,
                color = textColor
            )
        }

        Switch(
            modifier = Modifier.padding(start = 8.dp),
            checked = isChecked.value,
            onCheckedChange = {
                isChecked.value = it
                onSwitchChanges(it)
            },
            thumbContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                )
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color.Gray,
                uncheckedTrackColor = Color.Gray,
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent,
                checkedThumbColor = Color.Cyan,
                uncheckedThumbColor = Color.Magenta,
            )
        )
    }

}