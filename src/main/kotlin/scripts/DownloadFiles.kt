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
    private val user by option("--user", "-u",
            help = "Filters to files only by this user. " +
                    "Checks user IDs first, otherwise attempts to resolve the username then display name to ID")
    private val convo by option("--channel", "-c",
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
            // User ID first
            if (slack.users.containsKey(optionStr)) {
                optionStr
            } else {
                // Username if it exists
                slack.users.asSequence().firstOrNull {
                    it.value.username() == optionStr
                }?.key ?: run {
                    // Else try displayname
                    slack.users.asSequence().firstOrNull {
                        it.value.displayname() == optionStr
                    }?.key
                }
            }
        }

        val convoID = convo?.let { optionStr ->
            if (slack.conversations.containsKey(optionStr)) {
                optionStr
            } else {
                slack.conversations.asSequence().firstOrNull {
                    it.value.fullName(slack) == optionStr
                }?.key
            }
        }
        println(userID)
        println(convoID)

        val parsedFiles = slack.api.getFiles(
                startTime = timeOptions.startTime?.toEpochSecond(),
                endTime = timeOptions.endTime?.toEpochSecond(),
                user = userID,
                channel = convoID
        )
        val completeFiles = parsedFiles.toCompleteFiles(slack).filesByConvo()
        completeFiles.downloadFiles(slack, Paths.get("files"), slack.api)
    }

}