package com.posite.my_alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.posite.my_alarm.ui.theme.MyAlarmTheme
import com.posite.my_alarm.ui.time.TimePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            MyAlarmTheme {
                TimePicker()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewPicker() {
    MyAlarmTheme {
        TimePicker()
    }
}

