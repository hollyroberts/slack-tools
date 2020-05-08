package slack

import slackjson.CompleteFile
import slackjson.ParsedFile
import slackjson.SlackFile
import slackjson.SlackFile.FormattingType
import utils.DownloadStats
import utils.Log
import utils.WebApi
import utils.ensureFolderExists
import java.nio.file.Path

private object Files {
    const val LOCATION_INTERVAL = 3000
}

fun List<ParsedFile>.toCompleteFiles() : Map<String, CompleteFile> {
    // Start timer
    Log.high("Locating upload location of files (this may take a while, especially if inference is disabled)")
    val startTime = System.currentTimeMillis()
    var nextOutputTime = startTime + Files.LOCATION_INTERVAL

    // Iterate over objects, create map of file id to file objects
    val files = mutableMapOf<String, CompleteFile>()
    for ((index: Int, pf: ParsedFile) in this.withIndex()) {
        // Convert file to complete file
        files[pf.id] = pf.toCompleteFileByInference()

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

/**
 * Map of UserID --> list of files
 */
fun List<SlackFile>.filesByUser() : Map<String?, List<SlackFile>> {
    val filesUser = mutableMapOf<String?, MutableList<SlackFile>>()

    this.forEach {
        filesUser.getOrPut(it.user) { mutableListOf() }.add(it)
    }

    return filesUser.toMap()
}

/**
 * Takes a map of UserID --> List of files and downloads them into folders by displayname
 */
fun <F : SlackFile> Map<String?, List<F>>.downloadByUser(slack: SlackData, outDir: Path, webApi: WebApi?) {
    val mappedKeys = this.mapKeys { slack.userDisplayname(it.key) }
    mappedKeys.downloadToFolders(outDir, webApi, formatting = FormattingType.WITHOUT_NAME)
}

/**
 * Takes a map of ConvoID --> List of files and downloads them into folders by channel name
 */
fun Map<String?, List<CompleteFile>>.downloadByChannel(slack: SlackData, outDir: Path, webApi: WebApi?) {
    val mappedKeys = this.mapKeys { slack.conversationName(it.key) }
    mappedKeys.downloadToFolders(outDir, webApi)
}

fun <F : SlackFile> Map<String, List<F>>.downloadToFolders(outDir: Path, webApi: WebApi?,
                                                           formatting: FormattingType? = null) {
    // Process conversations alphabetically
    Log.high("Downloading files to '$outDir'")
    var downloadStats = DownloadStats()

    this.keys.sorted().forEach { key ->
        val filesInConvo = this.getValue(key)
        val folder = outDir.resolve(key)
        ensureFolderExists(folder)

        // Download files
        val channelStats = DownloadStats()
        Log.medium("Downloading ${filesInConvo.size} files from $key")
        filesInConvo.sortedBy { it.timestamp }.forEach { file ->
            channelStats.update(file.download(folder, webApi, formatting = formatting))
        }

        channelStats.log(key, Log.Modes.MEDIUM)
        downloadStats += channelStats
    }

    downloadStats.log("slack", Log.Modes.HIGH)
}