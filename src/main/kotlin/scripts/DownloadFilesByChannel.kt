package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import slack.*
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import slackjson.ConversationTypes
import utils.Http
import java.io.File
import java.nio.file.Paths

fun main(args: Array<String>) = ScriptDownloadByChannel().main(args)

class ScriptDownloadByChannel: CliktCommand(
        name = "download-files-by-channel"
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
    private val user by option("--user", "-u",
            help = "Filters to files only by this user. " +
                    "Checks user IDs first, otherwise attempts to resolve the username then display name to ID")
    private val convo by option("--channel", "-c",
            help = "Filters files to those only by this channel. Can be public/private channel or DM. " +
                    "Checks channel IDs first, otherwise attempts to resolve the name (with #/@) to ID")

    private val convoTypes by option("--channel-type", "-ct",
            help = "The types of channels to include. Use ',' to separate types. By default all types are included",
            metavar = ConversationTypes.optionStr())
            .convert { inputStr ->
                inputStr.split(",").map { arg ->
                    ConversationTypes.values().find { arg.toLowerCase() == it.shortName }
                            ?: fail("Unknown channel type '$arg'\nAvailable options are: " + ConversationTypes.optionStr())
                }.toSet()
            }
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
        val userID = user?.let { slack.inferUserID(it) }
        val convoID = convo?.let { slack.inferChannelID(it) }

        val parsedFiles = slack.api.getFiles(
                startTime = timeOptions.startTime?.toEpochSecond(),
                endTime = timeOptions.endTime?.toEpochSecond(),
                user = userID,
                channel = convoID
        )
        var completeFiles = parsedFiles.toCompleteFiles(slack).filesByConvo()
        if (convoTypes != null) {
            completeFiles = completeFiles.filterKeys { convoTypes!!.contains(slack.conversationType(it)) }
        }
        completeFiles.downloadByChannel(slack, output.toPath(), slack.api)
    }
}