package com.posite.my_alarm.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.posite.my_alarm.ui.picker.DayOfWeek
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
    val dayOfWeeks: MutableList<DayOfWeek> = mutableListOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    ),
) : Parcelable

class DayOfWeekConverter {
    @TypeConverter
    fun listToJson(list: MutableList<DayOfWeek>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun jsonToList(value: String): MutableList<DayOfWeek> {
        return Gson().fromJson(value, Array<DayOfWeek>::class.java).toMutableList()
    }
}
