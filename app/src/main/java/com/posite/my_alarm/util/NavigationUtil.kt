package com.posite.my_alarm.util

sealed class Screen(val route: String) {
    object AlarmScreen : Screen("Alarm")
    object TimerScreen : Screen("Timer")
}

sealed class BottomNavItem(val drawable: Int, val screen: Screen) {
    data object Alarm : BottomNavItem(com.posite.my_alarm.R.drawable.alarm, Screen.AlarmScreen)
    data object Timer : BottomNavItem(com.posite.my_alarm.R.drawable.timer, Screen.TimerScreen)
}