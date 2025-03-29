package com.posite.my_alarm.ui.time

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posite.my_alarm.R
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.picker.Picker

@Composable
fun TimePicker(
    meridiemState: PickerState<String>,
    hourState: PickerState<Int>,
    minuteState: PickerState<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Picker(
            modifier = Modifier
                .width(60.dp)
                .height(120.dp),
            items = listOf("오전", "오후"),
            state = meridiemState,
            textStyle = TextStyle(fontSize = 24.sp),
            selectedTextStyle = TextStyle(fontSize = 24.sp),
            visibleItemsCount = 2,
            isInfinity = false,
        )

        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(0.dp)
        )

        Picker(
            modifier = Modifier.width(40.dp),
            items = (0..12).toList(),
            state = hourState,
            startIndex = 6,
            textStyle = TextStyle(fontSize = 24.sp),
            selectedTextStyle = TextStyle(fontSize = 24.sp),
            visibleItemsCount = 3,
        )

        Text(
            text = ":",
            style = TextStyle(fontSize = 24.sp),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Picker(
            modifier = Modifier.width(40.dp),
            items = (0..60).map { stringResource(R.string.number_format, it) }.toList(),
            state = minuteState,
            textStyle = TextStyle(fontSize = 24.sp),
            selectedTextStyle = TextStyle(fontSize = 24.sp),
            visibleItemsCount = 3,
        )
    }
}