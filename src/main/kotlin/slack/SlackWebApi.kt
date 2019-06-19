package slack

import slackjson.CompleteFile
import utils.*
import java.nio.file.Path

class SlackWebApi(token: String, settings: Settings) : SlackData(settings) {
    private val LOCATION_INTERVAL = 3000

    private val api = WebApi(token)
    
    override val conversations by lazy { api.getConversations() }
    override val filesParsed by lazy { api.getFiles() }
    override val users by lazy { api.getUsers() }

    override val filesComplete by lazy {
        // Get files parsed first so we do things 'in order'
        val filesParsed = filesParsed

        // Start timer
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
}