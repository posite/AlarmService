package com.posite.my_alarm.ui.picker

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.theme.MyAlarmTheme


@Composable
fun TimePickerDialog(
    modifier: Modifier,
    properties: DialogProperties,
    meridiemState: PickerState<String>,
    hourState: PickerState<Int>,
    minuteState: PickerState<String>,
    meridiem: String,
    hour: Int,
    minute: Int,
    selectedDayOfWeek: MutableSet<DayOfWeek>,
    onDismissRequest: () -> Unit,
    onDoneClickListener: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier
                .padding(24.dp, 0.dp)
                .clip(shape = RoundedCornerShape(10.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    TimePicker(
                        meridiemState,
                        hourState,
                        minuteState,
                        meridiem,
                        hour,
                        minute
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                DayOfWeekScreen(selectedDayOfWeek)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        modifier = Modifier,
                        onClick = onDismissRequest,
                    ) {
                        Text("Cancel")
                        selectedDayOfWeek.clear()
                    }

                    TextButton(
                        modifier = Modifier,
                        onClick = {
                            Log.d("TimePickerDialog", selectedDayOfWeek.toString())
                            onDoneClickListener()
                        },
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun TimePickerDialogPreview() {
    MyAlarmTheme {
//        TimePickerDialog(
//            properties = DialogProperties(
//                dismissOnBackPress = true,
//                dismissOnClickOutside = true,
//            ),
//            onDismissRequest = {},
//            onDoneClickListener = {
//
//            },
//            meridiemState = PickerState(stringResource(R.string.am)),
//            hourState = PickerState(6),
//            minuteState = PickerState("00"),
//            meridiem = stringResource(R.string.am),
//            hour = 6,
//            minute = 0,
//        )
    }
}