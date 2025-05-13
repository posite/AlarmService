package com.posite.my_alarm.util.permission

interface Permission {
    fun request()
    fun isGranted(): Boolean
}