package com.posite.my_alarm.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.posite.my_alarm.data.entity.AlarmStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmStateDao {
    @Query("SELECT * FROM alarm_table ORDER BY meridiem, hour, minute ASC")
    fun getAllAlarmStates(): Flow<List<AlarmStateEntity>>

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    fun getAlarmStateById(id: Long): Flow<AlarmStateEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAlarmState(alarmState: AlarmStateEntity)

    @Update
    suspend fun updateAlarmState(alarmState: AlarmStateEntity)

    @Delete
    suspend fun deleteAlarmState(alarmState: AlarmStateEntity)

    @Delete
    suspend fun deleteAlarmStates(alarmStates: List<AlarmStateEntity>)
}