package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import slackjson.User
import utils.*
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

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
    // Output information
    private val output by option("--output", "-o",
            help = "Location to output files")
            .file(fileOkay = false)
            .default(File("avatars"))
    private val useDisplayname by option("--displayname", "-dn",
            help = "Use the display name instead of username").flag()
    private val includeDate by option("--add-date", "-ad",
            help = "Adds the date in format ' - yyyy\\.mm\\.dd' to the folder").flag()

    private val includeBots by option("--include-bots", "-ib",
            help = "Download the avatar images of bots").flag()
    private val includeDeleted by option("--include-deleted", "-id",
            help = "Download avatars for deactivated accounts").flag()

    override fun run() {
        // Fetch additional options
        topLevelOptions.run()
        val timeOptions = timeOptionsParser.options()

        // Get users
        val users = WebApi(token).getUsers().entries.filter { mapEntry ->
            if (!includeDeleted && mapEntry.value.deleted) {
                false
            } else !(!includeBots && mapEntry.value.isBot())
        }.sortedBy { getName(useDisplayname, it.value) }

        // Setup folder
        val outDir = if (includeDate) {
            val timeNow = Instant.now().atZone(timeOptions.outTz)
            val folderTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
            val extraStr = " - " + folderTimeFormatter.format(timeNow)
            output.toPath().resolveSibling(output.name + extraStr)
        } else {
            output.toPath()
        }
        ensureFolderExists(outDir)

        Log.high("Downloading avatars")
        val http = Http()
        users.forEach { mapEntry ->
            val url = mapEntry.value.profile.getLargestImage()
            val name = getName(useDisplayname, mapEntry.value)
            val saveLoc = outDir.resolve(name + guessImageExtFromURL(url))

            http.downloadFile(url, saveLoc, ignoreIfExists = true)
        }
        Log.high("Avatars downloaded")
    }

    private fun getName(useDisplayname: Boolean, user: User) = if (useDisplayname) {
        user.displayname()
    } else {
        user.username()
    }
}