package slack

import slackjson.CompleteFile
import utils.*
import java.nio.file.Path

class SlackWebApi(private val token: String, settings: Settings) : SlackData(settings) {
    private val LOCATION_INTERVAL = 3000

    private val api = WebApi(token)
    
    override val conversations by lazy { api.getConversations() }
    override val filesParsed by lazy { api.getFiles() }
    override val users by lazy { api.getUsers() }

    override val filesComplete by lazy {
        // Get files parsed first so we do things 'in order'
        val filesParsed = filesParsed

        Log.high("Locating upload location of files (this may take a while, especially if inference is disabled)")
        val startTime = System.currentTimeMillis()
        var nextOutputTime = startTime + LOCATION_INTERVAL

        // Iterate over objects, create map of file id to file objects
        val files = mutableMapOf<String, CompleteFile>()
        for ((index, pf) in filesParsed.withIndex()) {
            // Convert file to complete file
            files[pf.id] = pf.toCompleteFileByInference(api)

            // Print out how many objects have been processed
            if (System.currentTimeMillis() > nextOutputTime) {
                Log.medium("Processed ${index + 1}/${files.size} files")
                nextOutputTime = System.currentTimeMillis() + LOCATION_INTERVAL
            }
        }

        // Output timed messages if took more than LOCATION_INTERVAL
        val timeTaken = System.currentTimeMillis() - startTime
        if (timeTaken > LOCATION_INTERVAL) {
            Log.high(String.format("Located the upload location of all files in %,.1f seconds", timeTaken.toFloat() / 1000))
        } else {
            Log.medium("Files located")
        }

        return@lazy files.toMap()
    }

    // Download methods
    // TODO move into SlackData, as this should be possible for both the API and export
    fun downloadFiles(outDir: Path) {
        // Process conversations alphabetically
        Log.high("Downloading files")
        var downloadStats = DownloadStats()
        filesByConvo.keys.sortedBy { getConversationName(it) }.forEach { convoID ->
            val filesInConvo = filesByConvo[convoID]!!

            // Get location to put files in
            val convoName = getConversationName(convoID)
            val convoFolder = outDir.resolve(convoName)

            // Create folder if it doesn't exist
            ensureFolderExists(convoFolder)

            // Download files
            val channelStats = DownloadStats()
            Log.high("Downloading ${filesInConvo.size} files from $convoName")
            filesInConvo.sortedBy { it.timestamp }.forEach { file ->
                channelStats.update(file.download(convoFolder, this, Http(token)))
            }

            channelStats.log(convoName, Log.Modes.HIGH)
            downloadStats += channelStats
        }

        downloadStats.log("slack", Log.Modes.HIGH)
    }

    // TODO move into SlackData, as this should be possible for both the API and export
    fun downloadAvatars(outDir: Path, ignoreDeleted: Boolean = true, ignoreBots: Boolean = true) {
        ensureFolderExists(outDir)
        val avatarURLs = users.mapValues { it.value.profile.getLargestImage() }
        val http = Http(token)

        Log.high("Downloading avatars")

        // Filter users to process, then sort alphabetically
        users.entries.filter { mapEntry ->
            if (ignoreDeleted && mapEntry.value.deleted) {
                false
            } else !(ignoreBots && mapEntry.value.is_bot)
        }.sortedBy { it.value.name }.forEach { mapEntry ->
            val url = avatarURLs[mapEntry.key]!!
            val saveLoc = outDir.resolve(mapEntry.value.name + guessImageExtFromURL(url))

            http.downloadFile(url, saveLoc, ignoreIfExists = true, addAuthToken = false)
        }

        Log.high("Avatars downloaded")
    }
}