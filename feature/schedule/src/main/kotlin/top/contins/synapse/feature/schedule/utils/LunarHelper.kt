package top.contins.synapse.feature.schedule.utils

import com.nlf.calendar.Lunar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

object LunarHelper {
    val lunarCache = ConcurrentHashMap<LocalDate, String>()
    private val loadedLunarYears = ConcurrentHashMap.newKeySet<Int>()

    suspend fun preloadLunarYear(year: Int) {
        if (loadedLunarYears.contains(year)) return

        // Cache Recycle Mechanism: Limit to ~20 years to save memory
        if (loadedLunarYears.size > 20) {
            val yearsToRemove = loadedLunarYears.filter { kotlin.math.abs(it - year) > 15 }.toSet()
            if (yearsToRemove.isNotEmpty()) {
                loadedLunarYears.removeAll(yearsToRemove)
                val iterator = lunarCache.keys.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next().year in yearsToRemove) {
                        iterator.remove()
                    }
                }
            }
        }
        
        loadedLunarYears.add(year)

        withContext(Dispatchers.Default) {
            val start = LocalDate.of(year, 1, 1)
            val end = LocalDate.of(year, 12, 31)
            var current = start
            while (!current.isAfter(end)) {
                if (!lunarCache.containsKey(current)) {
                    try {
                        val d = Date.from(current.atStartOfDay(ZoneId.systemDefault()).toInstant())
                        val lunar = Lunar.fromDate(d)
                        lunarCache[current] = lunar.dayInChinese
                    } catch (_: Exception) {
                        lunarCache[current] = ""
                    }
                }
                current = current.plusDays(1)
            }
        }
    }
}
