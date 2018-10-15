package slack

import scripts.DownloadStats
import slackjson.CompleteFile
import utils.Api
import utils.Http
import utils.Log
import utils.ensureFolderExists
import java.nio.file.Path

class SlackDataFromApi(private val token: String, settings: Settings) : SlackData(settings) {
    private val LOCATION_INTERVAL = 3000

    private val api = Api(token)
    
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
        for ((index, obj) in filesParsed.withIndex()) {
            // Cast file and add to list
            val cf = CompleteFile(obj, api, true)
            files[cf.id] = cf

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
}

data class Settings(
        val inferFileLocation: Boolean = true,
        val ignoreDownloadedFiles: Boolean = true
)