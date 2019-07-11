package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import slack.Settings
import slack.SlackWebApi
import slack.downloadByUser
import slack.filesByUser
import utils.Http
import utils.Log
import java.io.File

fun main(args: Array<String>) = ScriptDownloadByUser().main(args)

class ScriptDownloadByUser : CliktCommand(
        name = "download-files-by-user"
) {
    // Top level options
    private val topLevelOptions by TopLevelOptions()
    private val timeOptionsParser by TimeOptions()

    // Auth
    private val token by option("--token", "-t",
            envvar = "SlackToken",
            help = "Authorisation token for slacks web api"
    ).required()

    // Options
    private val convo by argument(name = "channel",
            help = "Filters files to those only by this channel. Can be public/private channel or DM. " +
                    "Checks channel IDs first, otherwise attempts to resolve the name (with #/@) to ID")
    private val output by option("--output", "-o",
            help = "Location to output files")
            .file(fileOkay = false)
            .default(File("files"))

    override fun run() {
        // Fetch additional options
        topLevelOptions.run()
        val timeOptions = timeOptionsParser.options()

        // Setup
        val settings = Settings(fileConflictStrategy = Http.ConflictStrategy.HASH).applyTimeOptions(timeOptions)
        val slack = SlackWebApi(token, settings)

        // Resolve user/conversation ID
        val convoID = convo.let { slack.inferChannelID(it) } ?: run {
            Log.error("Could not infer channel from '$convo'")
            return
        }

        val parsedFiles = slack.api.getFiles(
                startTime = timeOptions.startTime?.toEpochSecond(),
                endTime = timeOptions.endTime?.toEpochSecond(),
                channel = convoID
        )

        val filesByUser = parsedFiles.filesByUser()
        filesByUser.downloadByUser(slack, output.toPath(), slack.api)
    }
}