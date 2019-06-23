package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import slack.*
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import java.nio.file.Paths

fun main(args: Array<String>) = ScriptDownloadFiles().main(args)

class ScriptDownloadFiles : CliktCommand(
        name = "download-files"
) {
    // Top level options
    private val topLevelOptions by TopLevelOptions()
    private val timeOptionsParser by TimeOptions()

    // Auth
    private val token by argument(
            help = "Authorisation token for slacks web api"
    )

    // Options
    // TODO user, channel, channel type
    val user by option("--user", "-u",
            help = "Filters to files only by this user. " +
                    "Checks user IDs first, otherwise attempts to resolve the username to ID")
    val convo by option("--channel", "-c",
            help = "Filters files to those only by this channel. Can be public/private channel or DM" +
                    "Checks channel IDs first, otherwise attempts to resolve the name (with #/@) to ID")

    override fun run() {
        // Fetch additional options
        topLevelOptions.run()
        val timeOptions = timeOptionsParser.options()

        // Setup
        val settings = Settings()
        val slack = SlackWebApi(token, settings)

        // Resolve user/conversation ID
        val userID = user?.let { optionStr ->
            if (slack.users.containsKey(optionStr)) {
                optionStr
            } else {
                slack.users.asSequence().filter {
                    it.value.name == optionStr
                }.first().key
            }
        }

        val parsedFiles = slack.api.getFiles(
                startTime = timeOptions.startTime?.toEpochSecond(),
                endTime = timeOptions.endTime?.toEpochSecond()
        )
        val completeFiles = parsedFiles.toCompleteFiles(slack).filesByConvo()
        completeFiles.downloadFiles(slack, Paths.get("files"), slack.api)
    }

}