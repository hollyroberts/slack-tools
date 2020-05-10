package scripts

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parsers.OptionParser
import org.apache.logging.log4j.kotlin.Logging
import utils.Log
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * Contains internally the string representations of parsed options
 * Converted options can be retrieved with options()
 */
class TimeOptions : OptionGroup(
        name = "Time options",
        help = "Options for controlling how time is used. " +
                "DATETIME is the format used (eg. dd/MM/yy)"
) {
    companion object : Logging {
        private val dtf = DateTimeFormatterBuilder()
                .appendPattern("dd/MM/[yyyy][yy]")
                .optionalStart()
                .appendPattern(" HH:mm")
                .optionalEnd()
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .toFormatter()!!
    }

    // Date input options
    private val printTzs by option("--timezone-list", "-tzl",
            help = "Prints out the available timezones and the current timezone").flag()
    private val inputTzOption by option("--timezone-input", "-tzi",
            help = "The timezone for the datetimes given via arguments")
    private val outTzOption by option("--timezone-output", "-tzo",
            help = "The timezone for any information output (eg. message/file times)")

    // Start/end time
    private val startTimeOption by option("--start-time", "-ts",
            help = "Include anything after this time (inclusive)",
            metavar = "DATETIME")
    private val endTimeOptions by option("--end-time", "-te",
            help = "Exclude anything after this time (exclusive)",
            metavar = "DATETIME")

    // Parsed and transformed variables
    var startTime: ZonedDateTime? = null
    var endTime: ZonedDateTime? = null
    lateinit var inputTz: ZoneId
    lateinit var outputTz: ZoneId

    override fun finalize(context: Context, invocationsByOption: Map<Option, List<OptionParser.Invocation>>) {
        super.finalize(context, invocationsByOption)

        // Zone information
        if (printTzs) {
            logger.log(Log.HIGH) { "Available timezones: " }
            logger.log(Log.HIGH) { ZoneId.getAvailableZoneIds().sorted().joinToString("\n       ") }
            logger.log(Log.HIGH) { "Current timezone: " + ZoneId.systemDefault() }
        }

        // Input/output zone
        inputTz = if (inputTzOption != null) {
            ZoneId.of(inputTzOption)
        } else {
            ZoneId.systemDefault()
        }
        outputTz = if (outTzOption != null) {
            ZoneId.of(outTzOption)
        } else {
            ZoneId.systemDefault()
        }

        // Parse datetime
        startTime = startTimeOption?.let {
            val startTimeLocal = LocalDateTime.parse(it, dtf)
            ZonedDateTime.of(startTimeLocal, inputTz)
        }
        endTime = endTimeOptions?.let {
            val endTimeLocal = LocalDateTime.parse(it, dtf)
            ZonedDateTime.of(endTimeLocal, outputTz)
        }
    }
}