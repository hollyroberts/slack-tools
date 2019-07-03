package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import utils.*
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
    // Output information
    private val output by option("--output", "-o",
            help = "Location to output files")
            .file(fileOkay = false)
            .default(File("avatars"))
    private val useDisplayname by option("--displayname", "-dn",
            help = "Use the display name instead of username").flag()

    private val includeBots by option("--include-bots", "-ib",
            help = "Download the avatar images of bots").flag()
    private val includeDeleted by option("--include-deleted", "-id",
            help = "Download avatars for deactivated accounts").flag()

    override fun run() {
        // Fetch additional options
        topLevelOptions.run()

        // Get users
        val users = WebApi(token).getUsers().entries.filter { mapEntry ->
            if (!includeDeleted && mapEntry.value.deleted) {
                false
            } else !(!includeBots && mapEntry.value.isBot)
        }.sortedBy { it.value.username() }

        // Setup
        val outDir = output.toPath()
        ensureFolderExists(outDir)
        val http = Http()

        Log.high("Downloading avatars")
        users.forEach { mapEntry ->
            val url = mapEntry.value.profile.getLargestImage()
            val name = if (useDisplayname) {
                mapEntry.value.displayname()
            } else {
                mapEntry.value.username()
            }

            val saveLoc = outDir.resolve(name + guessImageExtFromURL(url))

            http.downloadFile(url, saveLoc, ignoreIfExists = true)
        }
        Log.high("Avatars downloaded")
    }
}