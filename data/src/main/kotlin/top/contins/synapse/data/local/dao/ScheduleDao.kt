package top.contins.synapse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import top.contins.synapse.data.local.entity.ScheduleEntity

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: String): @JvmSuppressWildcards ScheduleEntity?

    @Query("SELECT * FROM schedules WHERE start_time_utc >= :start AND end_time_utc <= :end")
    fun getSchedulesInTimeRange(start: Long, end: Long): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE calendar_id = :calendarId AND start_time_utc >= :start AND end_time_utc <= :end")
    fun getSchedulesInTimeRangeForCalendar(calendarId: String, start: Long, end: Long): Flow<List<ScheduleEntity>>

    // Conflict detection: (start1 < end2) AND (end1 > start2)
    @Query("SELECT * FROM schedules WHERE start_time_utc < :end AND end_time_utc > :start")
    suspend fun getConflictingSchedules(start: Long, end: Long): @JvmSuppressWildcards List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchSchedules(query: String): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): @JvmSuppressWildcards Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity): @JvmSuppressWildcards Int

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity): @JvmSuppressWildcards Int

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: String): @JvmSuppressWildcards Int

    @Query("DELETE FROM schedules WHERE calendar_id = :calendarId")
    suspend fun deleteSchedulesByCalendarId(calendarId: String): @JvmSuppressWildcards Int
}
