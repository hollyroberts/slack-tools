package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import slack.*
import utils.Log
import java.nio.file.Paths
import java.time.Instant

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import java.time.ZonedDateTime

fun main(args: Array<String>) = ScriptDownloadFiles().main(args)

class ScriptDownloadFiles : CliktCommand(
        name="download-files"
) {
    // Arguments/options
    private val topLevelArgs by TopLevelArgs()
    private val token by argument(
            help="Authorisation token for slacks web api"
    )

    override fun run() {
        topLevelArgs.run()
        val settings = Settings()
        val slack = SlackWebApi(token, settings)

        // TODO
        Log.high(Instant.now().minusSeconds(86_400).epochSecond.toString())

        val parsedFiles = slack.api.getFiles(startTime = Instant.now().minusSeconds(86_400).epochSecond)
        val completeFiles = parsedFiles.toCompleteFiles(slack).filesByConvo()
        completeFiles.downloadFiles(slack, Paths.get("files"), slack.api)
    }

}

class TimeArgs : OptionGroup(
        name="Time options",
        help="Options for controlling how time is used"
) {
    private val startTime by option("--start-time", "-ts",
            help="Include anything after this time (inclusive)")
    private val endTime by option("--end-time", "-te",
            help="Exclude anything after this time (exclusive)")
}

class TopLevelArgs : OptionGroup(
        name="Top level options",
        help="Options that are common across all scripts") {

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