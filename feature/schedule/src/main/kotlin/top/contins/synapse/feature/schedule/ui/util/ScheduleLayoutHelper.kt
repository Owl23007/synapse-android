package top.contins.synapse.feature.schedule.ui.util

import top.contins.synapse.domain.model.schedule.Schedule

data class PositionedSchedule(
    val schedule: Schedule,
    val colIndex: Int,
    val totalCols: Int
)

object ScheduleLayoutHelper {
    /**
     * Arrange schedules into columns for day view.
     * Overlapping schedules will share the width.
     */
    fun arrangeDaySchedules(schedules: List<Schedule>): List<PositionedSchedule> {
        if (schedules.isEmpty()) return emptyList()

        // 1. Sort by start time, then end time (longer first if start times equal)
        val sorted = schedules.sortedWith(compareBy({ it.startTime }, { -it.endTime }))
        
        // 2. Group into disjoint clusters based on overlap
        val clusters = mutableListOf<MutableList<Schedule>>()
        if (sorted.isNotEmpty()) {
            var currentCluster = mutableListOf<Schedule>()
            currentCluster.add(sorted[0])
            clusters.add(currentCluster)
            
            var clusterEnd = sorted[0].endTime
            
            for (i in 1 until sorted.size) {
                val s = sorted[i]
                if (s.startTime < clusterEnd) {
                    currentCluster.add(s)
                    clusterEnd = maxOf(clusterEnd, s.endTime)
                } else {
                    currentCluster = mutableListOf()
                    currentCluster.add(s)
                    clusters.add(currentCluster)
                    clusterEnd = s.endTime
                }
            }
        }

        // 3. Arrange each cluster
        val result = mutableListOf<PositionedSchedule>()
        
        for (cluster in clusters) {
            val columns = mutableListOf<MutableList<Schedule>>()
            // To properly calculate efficient packing, we iterate and try to fit into existing columns
            for (s in cluster) {
                var placed = false
                for (col in columns) {
                   val lastInCol = col.last()
                   // If this schedule starts after the last one in this column ends, place it here
                   if (s.startTime >= lastInCol.endTime) {
                       col.add(s)
                       placed = true
                       break
                   }
                }
                if (!placed) {
                    columns.add(mutableListOf(s))
                }
            }
            
            // All items in this connected component share the total width
            val totalCols = columns.size
            columns.forEachIndexed { index, list ->
                list.forEach { s ->
                    result.add(PositionedSchedule(s, index, totalCols))
                }
            }
        }
        
        return result
    }
}
