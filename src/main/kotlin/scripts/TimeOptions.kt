package scripts

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.option
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Contains internally the string representations of parsed options
 * Converted options can be retrieved with options()
 */
class TimeOptions : OptionGroup(
        name = "Time options",
        help = "Options for controlling how time is used. " +
                "DATETIME is the format used (eg. dd/MM/yy)"
) {
    data class Options(
            val startTime: ZonedDateTime?,
            val endTime: ZonedDateTime?
    )

    private val dtf = DateTimeFormatter.ofPattern("dd/MM/yy")
    private val startTimeStr by option("--start-time", "-ts",
            help = "Include anything after this time (inclusive)",
            metavar = "DATETIME")
    private val endTimeStr by option("--end-time", "-te",
            help = "Exclude anything after this time (exclusive)",
            metavar = "DATETIME")

    /**
     * Return the data class of parsed options
     */
    fun options(): Options {
        val timeZone = ZoneId.systemDefault()

        val startTime = startTimeStr?.let {
            val startTimeLocal = LocalDateTime.parse(it, dtf)
            ZonedDateTime.of(startTimeLocal, timeZone)
        }
        val endTime = endTimeStr?.let {
            val endTimeLocal = LocalDateTime.parse(it, dtf)
            ZonedDateTime.of(endTimeLocal, timeZone)
        }

        return Options(startTime, endTime)
    }
}