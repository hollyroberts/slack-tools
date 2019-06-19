package slack

import slackjson.CompleteFile
import slackjson.ParsedFile
import utils.DownloadStats
import utils.Log
import utils.WebApi
import utils.ensureFolderExists
import java.nio.file.Path

private object Files {
    const val LOCATION_INTERVAL = 3000
}

fun List<ParsedFile>.toCompleteFiles(slackWebApi: SlackWebApi) : Map<String, CompleteFile> {
    // Start timer
    Log.high("Locating upload location of files (this may take a while, especially if inference is disabled)")
    val startTime = System.currentTimeMillis()
    var nextOutputTime = startTime + Files.LOCATION_INTERVAL

    // Iterate over objects, create map of file id to file objects
    val files = mutableMapOf<String, CompleteFile>()
    for ((index: Int, pf: ParsedFile) in this.withIndex()) {
        // Convert file to complete file
        files[pf.id] = pf.toCompleteFileByInference(slackWebApi.api)

        // Print out how many objects have been processed
        if (System.currentTimeMillis() > nextOutputTime) {
            Log.medium("Processed ${index + 1}/${files.size} files")
            nextOutputTime = System.currentTimeMillis() + Files.LOCATION_INTERVAL
        }
    }

    // Output timed messages if took more than LOCATION_INTERVAL
    val timeTaken = System.currentTimeMillis() - startTime
    if (timeTaken > Files.LOCATION_INTERVAL) {
        Log.high(String.format("Located the upload location of all files in %,.1f seconds", timeTaken.toFloat() / 1000))
    } else {
        Log.medium("Files located")
    }

    return files.toMap()
}

/**
 * Map of conversation id --> list of files
 * Files without an identified conversation go to null string
 */
fun Map<String, CompleteFile>.filesByConvo() : Map<String?, List<CompleteFile>> {
    val filesConvo = mutableMapOf<String?, MutableList<CompleteFile>>()

    this.values.forEach {
        // Key will be null if we don't know the convo
        filesConvo.getOrPut(it.uploadLoc) { mutableListOf() }.add(it)
    }

    return filesConvo.toMap()
}

fun Map<String?, List<CompleteFile>>.downloadFiles(slack: SlackData, outDir: Path, webApi: WebApi?) {
    // Process conversations alphabetically
    Log.high("Downloading files")
    var downloadStats = DownloadStats()
    this.keys.sortedBy { slack.getConversationName(it) }.forEach { convoID ->
        val filesInConvo = this.getValue(convoID)

        // Get location to put files in
        val convoName = slack.getConversationName(convoID)
        val convoFolder = outDir.resolve(convoName)

        // Create folder if it doesn't exist
        ensureFolderExists(convoFolder)

        // Download files
        val channelStats = DownloadStats()
        Log.high("Downloading ${filesInConvo.size} files from $convoName")
        filesInConvo.sortedBy { it.timestamp }.forEach { file ->
            channelStats.update(file.download(convoFolder, slack, webApi))
        }

        channelStats.log(convoName, Log.Modes.HIGH)
        downloadStats += channelStats
    }

    downloadStats.log("slack", Log.Modes.HIGH)
}