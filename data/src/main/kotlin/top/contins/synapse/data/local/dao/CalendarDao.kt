package top.contins.synapse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import top.contins.synapse.data.local.entity.CalendarEntity

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendars")
    fun getAllCalendars(): Flow<List<CalendarEntity>>

    @Query("SELECT * FROM calendars WHERE id = :id")
    suspend fun getCalendarById(id: String): @JvmSuppressWildcards CalendarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendar(calendar: CalendarEntity): @JvmSuppressWildcards Long

    @Update
    suspend fun updateCalendar(calendar: CalendarEntity): @JvmSuppressWildcards Int

    @Delete
    suspend fun deleteCalendar(calendar: CalendarEntity): @JvmSuppressWildcards Int

    @Query("DELETE FROM calendars WHERE id = :id")
    suspend fun deleteCalendarById(id: String): @JvmSuppressWildcards Int
}
