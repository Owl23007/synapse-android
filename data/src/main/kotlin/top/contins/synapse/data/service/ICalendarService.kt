package top.contins.synapse.data.service

import biweekly.Biweekly
import biweekly.ICalendar
import biweekly.component.VEvent
import biweekly.property.*
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.model.schedule.ScheduleType
import top.contins.synapse.domain.model.schedule.RepeatRule
import java.util.*
import javax.inject.Inject

/**
 * Service for converting between Schedule and iCalendar format
 * Uses biweekly library for iCalendar (RFC 5545) parsing and generation
 */
class ICalendarService @Inject constructor() {
    
    /**
     * Convert schedules to iCalendar format string
     * @param schedules List of schedules to export
     * @return iCalendar format string (RFC 5545)
     */
    fun exportToICalendar(schedules: List<Schedule>): String {
        val calendar = ICalendar()
        calendar.productId = ProductId("-//Synapse Android//Schedule Manager//EN")
        calendar.version = Version.v2_0()
        
        schedules.forEach { schedule ->
            val event = scheduleToVEvent(schedule)
            calendar.addEvent(event)
        }
        
        return Biweekly.write(calendar).go()
    }
    
    /**
     * Parse iCalendar content and convert to Schedule list
     * @param icsContent iCalendar format string
     * @param defaultCalendarId Calendar ID to assign to imported schedules
     * @param subscriptionId Optional subscription ID if importing from subscription
     * @return List of parsed schedules
     */
    fun importFromICalendar(
        icsContent: String,
        defaultCalendarId: String,
        subscriptionId: String? = null
    ): List<Schedule> {
        val calendars = Biweekly.parse(icsContent).all()
        val schedules = mutableListOf<Schedule>()
        
        calendars.forEach { calendar ->
            calendar.events.forEach { event ->
                try {
                    val schedule = vEventToSchedule(
                        event,
                        defaultCalendarId,
                        subscriptionId
                    )
                    schedules.add(schedule)
                } catch (e: Exception) {
                    // Log error but continue processing other events
                    android.util.Log.e("ICalendarService", "Failed to parse event: ${event.uid?.value}", e)
                }
            }
        }
        
        return schedules
    }
    
    private fun scheduleToVEvent(schedule: Schedule): VEvent {
        val event = VEvent()
        
        // Basic properties
        event.summary = Summary(schedule.title)
        if (!schedule.description.isNullOrEmpty()) {
            event.description = Description(schedule.description)
        }
        
        // Time properties
        val startDate = Date(schedule.startTime)
        val endDate = Date(schedule.endTime)
        
        if (schedule.isAllDay) {
            event.dateStart = DateStart(startDate, false)
            event.dateEnd = DateEnd(endDate, false)
        } else {
            event.dateStart = DateStart(startDate, true)
            event.dateEnd = DateEnd(endDate, true)
        }
        
        // Location
        if (!schedule.location.isNullOrEmpty()) {
            event.location = Location(schedule.location)
        }
        
        // UID
        event.uid = Uid(schedule.id)
        
        // Created and last modified
        event.created = Created(Date(schedule.createdAt))
        event.lastModified = LastModified(Date(schedule.updatedAt))
        
        // Categories (using schedule type)
        event.addCategories(schedule.type.name)
        
        return event
    }
    
    private fun vEventToSchedule(
        event: VEvent,
        calendarId: String,
        subscriptionId: String?
    ): Schedule {
        val uid = event.uid?.value ?: UUID.randomUUID().toString()
        val title = event.summary?.value ?: "Untitled Event"
        val description = event.description?.value
        
        val startTime = event.dateStart?.value?.time ?: System.currentTimeMillis()
        val endTime = event.dateEnd?.value?.time ?: (startTime + 3600000) // +1 hour default
        
        // Use biweekly's hasTime() method to detect all-day events properly
        val isAllDay = event.dateStart?.value?.let {
            // Check if the DateStart has time component
            event.dateStart?.hasTime() == false
        } ?: false
        
        val location = event.location?.value
        val timezoneId = TimeZone.getDefault().id
        
        val now = System.currentTimeMillis()
        
        return Schedule(
            id = uid,
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            timezoneId = timezoneId,
            location = location,
            type = ScheduleType.EVENT, // Default type
            color = null,
            reminderMinutes = null,
            isAlarm = false,
            repeatRule = null, // TODO: Parse recurrence rules
            calendarId = calendarId,
            isAllDay = isAllDay,
            isFromSubscription = subscriptionId != null,
            subscriptionId = subscriptionId,
            createdAt = event.created?.value?.time ?: now,
            updatedAt = event.lastModified?.value?.time ?: now
        )
    }
}
