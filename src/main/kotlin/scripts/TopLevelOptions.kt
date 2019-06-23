package scripts

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import utils.Log

class TopLevelOptions : OptionGroup(
        name = "Top level options",
        help = "Options that are common across all scripts") {

    companion object {
        val LOG_OPTIONS = Log.argStringMap().keys
    }

    private val logMode: Log.Modes? by option(
            "--log-mode", "-lm",
            metavar = LOG_OPTIONS.joinToString(", "),
            help = "The logging mode to be used. Prints out the available options if called")
            .convert {
                Log.argStringMap()[it.toUpperCase()] ?: run {
                    fail("Unknown log level '$it'\nAvailable options are: " + LOG_OPTIONS.joinToString(", "))
                }
            }

    /**
     * Function to run the setup from the parameters defined here
     * Not automatically run by moshi so has to be called manually
     */
    fun run() {
        logMode?.let { Log.mode = it }
    }
}