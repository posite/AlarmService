package com.posite.my_alarm.ui.picker

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posite.my_alarm.R
import com.posite.my_alarm.data.model.PickerState

@Composable
fun CountDownTimerPicker(
    hourState: PickerState<String>,
    minuteState: PickerState<String>,
    secondState: PickerState<String>
) {
    Row {
        Picker(
            modifier = Modifier.width(80.dp),
            items = (0..99).map { stringResource(R.string.number_format, it) }.toList(),
            state = hourState,
            startIndex = 0,
            textStyle = TextStyle(fontSize = 32.sp),
            selectedTextStyle = TextStyle(fontSize = 32.sp),
            visibleItemsCount = 3,
            lazyListState = null,
        )

        Text(
            text = ":",
            style = TextStyle(fontSize = 40.sp),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Picker(
            modifier = Modifier.width(80.dp),
            items = (0..59).map { stringResource(R.string.number_format, it) }.toList(),
            state = minuteState,
            textStyle = TextStyle(fontSize = 32.sp),
            selectedTextStyle = TextStyle(fontSize = 32.sp),
            visibleItemsCount = 3,
            lazyListState = null,
            startIndex = 0
        )

        Text(
            text = ":",
            style = TextStyle(fontSize = 40.sp),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Picker(
            modifier = Modifier.width(80.dp),
            items = (0..59).map { stringResource(R.string.number_format, it) }.toList(),
            state = secondState,
            textStyle = TextStyle(fontSize = 32.sp),
            selectedTextStyle = TextStyle(fontSize = 32.sp),
            visibleItemsCount = 3,
            lazyListState = null,
            startIndex = 0
        )
    }

}