package com.posite.my_alarm.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "alarm_table")
@Parcelize
data class AlarmStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val meridiem: String,
    val isActive: Boolean,
) : Parcelable
