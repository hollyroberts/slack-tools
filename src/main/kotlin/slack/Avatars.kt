package slack

import slackjson.User
import utils.Http
import utils.Log
import utils.ensureFolderExists
import utils.guessImageExtFromURL
import java.nio.file.Path

class Avatars(private val users: Map<String, User>) {
    constructor(slackWebApi: SlackWebApi) : this(slackWebApi.users)

    fun downloadAvatars(outDir: Path, ignoreDeleted: Boolean = true, ignoreBots: Boolean = true) {
        ensureFolderExists(outDir)
        val avatarURLs = users.mapValues { it.value.profile.getLargestImage() }
        val http = Http()

        Log.high("Downloading avatars")

        // Filter users to process, then sort alphabetically
        users.entries.filter { mapEntry ->
            if (ignoreDeleted && mapEntry.value.deleted) {
                false
            } else !(ignoreBots && mapEntry.value.isBot)
        }.sortedBy { it.value.username() }.forEach { mapEntry ->
            val url = avatarURLs.getValue(mapEntry.key)
            val saveLoc = outDir.resolve(mapEntry.value.username() + guessImageExtFromURL(url))

            http.downloadFile(url, saveLoc, ignoreIfExists = true)
        }

        Log.high("Avatars downloaded")
    }
}