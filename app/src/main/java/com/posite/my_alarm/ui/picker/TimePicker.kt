package com.posite.my_alarm.ui.picker

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posite.my_alarm.R
import com.posite.my_alarm.data.model.PickerState

@Composable
fun TimePicker(
    meridiemState: PickerState<String>,
    hourState: PickerState<Int>,
    minuteState: PickerState<String>,
    meridiem: String,
    hour: Int,
    minute: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        val visibleItemMiddle = remember { 3 / 2 }
        val startIndex = remember { if (meridiem == context.getString(R.string.pm)) 1 else 0 }
        val listMeridiemState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

        val adjustedHour = (1..12).toList()
        val listScrollMax = Int.MAX_VALUE
        val listScrollMiddle = remember { listScrollMax / 2 }
        val listStartIndex =
            remember { listScrollMiddle - listScrollMiddle % adjustedHour.size - visibleItemMiddle + hour - 1 }
        val listHourState = rememberLazyListState(initialFirstVisibleItemIndex = listStartIndex)



        LaunchedEffect(listHourState) {
            var prevIndex = listHourState.firstVisibleItemIndex
            Log.d("prev", "prev: $prevIndex")
            snapshotFlow { listHourState.isScrollInProgress }.collect { isScrolling ->
                if (isScrolling) {
                    snapshotFlow { listHourState.firstVisibleItemIndex }.collect { index ->
                        when {
                            index > prevIndex && hourState.selectedItem == 12 -> {
                                if (meridiemState.selectedItem == context.getString(R.string.am)) {
                                    listMeridiemState.animateScrollToItem(1)
                                } else {
                                    listMeridiemState.animateScrollToItem(0)
                                }
                            }

                            index < prevIndex && hourState.selectedItem == 11 -> {
                                if (meridiemState.selectedItem == context.getString(R.string.pm)) {
                                    listMeridiemState.animateScrollToItem(0)
                                } else {
                                    listMeridiemState.animateScrollToItem(1)
                                }
                            }
                        }
                        prevIndex = index
                    }

                }

            }
        }


        Picker(
            modifier = Modifier
                .width(60.dp)
                .height(120.dp),
            items = listOf(stringResource(R.string.am), stringResource(R.string.pm)),
            state = meridiemState,
            textStyle = TextStyle(fontSize = 24.sp),
            selectedTextStyle = TextStyle(fontSize = 24.sp),
            visibleItemsCount = 2,
            isInfinity = false,
            startIndex = if (meridiem == stringResource(R.string.pm)) 1 else 0,
            lazyListState = listMeridiemState
        )

        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(0.dp)
        )



        Picker(
            modifier = Modifier.width(40.dp),
            items = (1..12).toList(),
            state = hourState,
            startIndex = hour,
            textStyle = TextStyle(fontSize = 24.sp),
            selectedTextStyle = TextStyle(fontSize = 24.sp),
            visibleItemsCount = 3,
            lazyListState = listHourState
        )

        Text(
            text = ":",
            style = TextStyle(fontSize = 24.sp),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Picker(
            modifier = Modifier.width(40.dp),
            items = (0..59).map { stringResource(R.string.number_format, it) }.toList(),
            state = minuteState,
            textStyle = TextStyle(fontSize = 24.sp),
            selectedTextStyle = TextStyle(fontSize = 24.sp),
            visibleItemsCount = 3,
            lazyListState = null,
            startIndex = minute
        )
    }
}