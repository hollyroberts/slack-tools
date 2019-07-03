package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import utils.Log
import utils.WebApi
import java.io.File

fun main(args: Array<String>) = ScriptDownloadAvatars().main(args)

class ScriptDownloadAvatars : CliktCommand(
        name = "download-files-by-user"
) {
    // Top level options
    private val topLevelOptions by TopLevelOptions()

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
    private val includeBots by option("--include-bots", "-ib",
            help = "Download the avatar images of bots").flag()
    private val includeDeleted by option("--include-deleted", "-id",
            help = "Download avatars for deactivated accounts").flag()

    override fun run() {
        // Fetch additional options
        topLevelOptions.run()

        // Setup
        var users = WebApi(token).getUsers()
        if (!includeBots) {
            users = users.filter { !it.value.isBot }
        }
        if (!includeDeleted) {
            users = users.filter { !it.value.deleted }
        }

        // Resolve user/conversation ID
        users.forEach {
            Log.medium(it.value.username() + " - " + it.value.displayname())
        }
    }
}