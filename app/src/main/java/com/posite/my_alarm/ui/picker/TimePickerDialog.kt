package com.posite.my_alarm.ui.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.theme.MyAlarmTheme


@Composable
fun TimePickerDialog(
    modifier: Modifier = Modifier,
    properties: DialogProperties,
    meridiemState: PickerState<String>,
    hourState: PickerState<Int>,
    minuteState: PickerState<String>,
    onDismissRequest: () -> Unit,
    onDoneClickListener: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier.clip(
                shape = RoundedCornerShape(10.dp)
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "시간 선택",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Start
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                TimePicker(meridiemState, hourState, minuteState)
                HorizontalDivider(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        modifier = Modifier,
                        onClick = onDismissRequest,
                    ) {
                        Text("Cancel")
                    }

                    TextButton(
                        modifier = Modifier,
                        onClick = onDoneClickListener,
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
        TimePickerDialog(
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
            onDismissRequest = {},
            onDoneClickListener = {

            },
            meridiemState = PickerState("오전"),
            hourState = PickerState(6),
            minuteState = PickerState("00")
        )
    }
}