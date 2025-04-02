package com.posite.my_alarm.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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

@Composable
fun Alarm(alarm: AlarmStateEntity, onSwitchChanges: (Boolean) -> Unit) {
    val isChecked = remember { mutableStateOf(alarm.isActive) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(30.dp), color = Color(0xFFEEEEEE))
            .height(120.dp)
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val textColor = if (isChecked.value) Color.Black else Color.Gray
        Row(modifier = Modifier.wrapContentSize(), verticalAlignment = Alignment.Bottom) {
            Text(text = alarm.meridiem, fontSize = 16.sp, color = textColor)
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .height(0.dp),
                color = Color.Transparent
            )
            Text(
                text = stringResource(R.string.alarm_time, alarm.hour, alarm.minute),
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
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                uncheckedThumbColor = Color.White,
                checkedTrackColor = Color.Cyan,
                uncheckedTrackColor = Color.Gray
            )
        )
    }
}