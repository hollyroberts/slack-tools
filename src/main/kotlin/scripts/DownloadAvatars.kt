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
import utils.Log
import java.io.File

fun main(args: Array<String>) = ScriptDownloadAvatars().main(args)

class ScriptDownloadAvatars : CliktCommand(
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
    private val output by option("--output", "-o",
            help = "Location to output files")
            .file(fileOkay = false)
            .default(File("files"))
    private val includeBots by option("--include-bots")

    override fun run() {
        // Fetch additional options
        topLevelOptions.run()
        val timeOptions = timeOptionsParser.options()

        // Setup
        val settings = Settings(timeOptions)
        val slack = SlackWebApi(token, settings)

        // Resolve user/conversation ID
        slack.users.forEach {
            Log.medium(it.value.username() + " - " + it.value.displayname())
        }
    }
}