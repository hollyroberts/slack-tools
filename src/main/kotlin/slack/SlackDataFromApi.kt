@file:Suppress("MemberVisibilityCanBePrivate")

package slack

import slackjson.*
import utils.Api
import utils.Log

abstract class SlackData {
    // Constants
    private val LOCATION_INTERVAL = 3000

    // Raw data
    abstract val conversations: Map<String, Conversation>
    abstract val filesParsed: List<ParsedFile>
    abstract val users: Map<String, User>

    // Responses may need further processing
    /**
     * Map of file id to complete files
     */
    val filesComplete by lazy {
        Log.low("Locating upload location of files (this may take a while, especially if inference is disabled)")
        val startTime = System.currentTimeMillis()
        var nextOutputTime = startTime + LOCATION_INTERVAL

        // Iterate over objects, create map of file id to file objects
        val files = mutableMapOf<String, CompleteFile>()
        for ((index, obj) in filesParsed.withIndex()) {
            // Cast file and add to list
            val cf = CompleteFile(obj, true) // TODO fix this !FileCommand.noInfer)
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
            Log.low(String.format("Located the upload location of all files in %,.1f seconds", timeTaken.toFloat() / 1000))
        } else {
            Log.medium("Files located")
        }

        return@lazy files
    }

    /**
     * Map of conversation id --> list of files
     */
    val filesByChannel by lazy {
        val fileChannel = mutableMapOf<String, MutableList<CompleteFile>>()

        filesComplete.values.forEach {
            val uploadLoc = it.uploadLoc ?: "Unknown channel"
            fileChannel.getOrPut(uploadLoc) { mutableListOf() }
                    .add(it)
        }

        return@lazy fileChannel.toMap()
    }
}

class SlackDataFromApi(val token: String) : SlackData() {
    override val conversations by lazy { Api.getConversations() }
    override val filesParsed by lazy { Api.getFiles() }
    override val users by lazy { Api.getUsers() }
}