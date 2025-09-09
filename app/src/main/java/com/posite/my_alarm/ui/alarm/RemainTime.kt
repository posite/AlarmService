package com.posite.my_alarm.ui.alarm

data class RemainTime(
    val month: Int,
    val day: Int,
    val meridiem: String,
    val dayOfWeek: String,
    val minute: Int,
    val hour: Int,
    val remainHour: Int,
    val remainMinute: Int,
)
