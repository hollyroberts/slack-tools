package scripts

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import utils.Log
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

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
            val endTime: ZonedDateTime?,
            val outTz: ZoneId
    )

    private val dtf = DateTimeFormatter.ofPattern("dd/MM/yy")

    // Date input options
    private val printTzs by option("--timezone-list", "-tzl",
            help = "Prints out the available timezones and the current timezone").flag()
    private val inputTz by option("--timezone-input", "-tzi",
            help = "The timezone for the datetimes given via arguments")
    private val outputTz by option("--timezone-output", "-tzo",
            help = "The timezone for any information output (eg. message/file times)")

    // Start/end time
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
        // Zone information
        if (printTzs) {
            Log.high("Available timezones: ")
            Log.high(ZoneId.getAvailableZoneIds().sorted().joinToString("\n       "))
            Log.high("Current timezone: " + ZoneId.systemDefault())
        }

        // Input/output zone
        val timeZone = if (inputTz != null) {
            ZoneId.of(inputTz)
        } else {
            ZoneId.systemDefault()
        }
        val outputTimeZone = if (outputTz != null) {
            ZoneId.of(outputTz)
        } else {
            ZoneId.systemDefault()
        }

        // Parse datetime
        val startTime = startTimeStr?.let {
            val startTimeLocal = LocalDateTime.parse(it, dtf)
            ZonedDateTime.of(startTimeLocal, timeZone)
        }
        val endTime = endTimeStr?.let {
            val endTimeLocal = LocalDateTime.parse(it, dtf)
            ZonedDateTime.of(endTimeLocal, timeZone)
        }

        return Options(startTime, endTime, outputTimeZone)
    }
}