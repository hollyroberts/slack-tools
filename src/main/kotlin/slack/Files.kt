package slack

import slackjson.CompleteFile
import slackjson.ParsedFile
import utils.Log

fun List<ParsedFile>.toCompleteFiles(slackWebApi: SlackWebApi) : Map<String, CompleteFile> {
    @Suppress("LocalVariableName")
    val LOCATION_INTERVAL = 3000

    // Start timer
    Log.high("Locating upload location of files (this may take a while, especially if inference is disabled)")
    val startTime = System.currentTimeMillis()
    var nextOutputTime = startTime + LOCATION_INTERVAL

    // Iterate over objects, create map of file id to file objects
    val files = mutableMapOf<String, CompleteFile>()
    for ((index: Int, pf: ParsedFile) in this.withIndex()) {
        // Convert file to complete file
        files[pf.id] = pf.toCompleteFileByInference(slackWebApi.api)

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

    return files.toMap()
}