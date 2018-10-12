import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import slack.SlackDataFromApi
import slackjson.CompleteFile
import slackjson.SlackFile
import utils.Api
import utils.Http.token
import utils.Log

object FileCommand: CliktCommand(
        name = "file",
        help = "Downloads files from slack") {

    // Args/Opts
    val noInfer by option("-ni", "--no-infer",
            help = "Disable channel inference using list API data, as edge cases can exist. " +
                    "Instead performs an API call per file (slow)").flag()
    private val output by option("-o", "--output",
            help = "Output directory for files. Extends the base directory set").default("files")



    /**
     * Downloads all files from slack
     */
    override fun run() {
        SlackDataFromApi(token!!).users.forEach { t, u -> println(u.name) }
        return
        val conversations = Api.getConversations()
        conversations.forEach { _, convo -> println(convo.getFullName()) }
        val filesRaw: MutableList<SlackFile> = Api.getFiles().toMutableList()

        // Start timer to output process every X seconds
        Log.low("Locating upload location of files (this may take a while, especially if inference is disabled)")
        val startTime = System.currentTimeMillis()
        var nextOutputTime = startTime + LOCATION_INTERVAL

        // Iterate over objects, create map of file channel to list of file objects
        val files = mutableMapOf<String, MutableList<CompleteFile>>()
        for ((index, obj) in filesRaw.withIndex()) {
            // Cast file and add to list
            val cf = CompleteFile(obj, !noInfer)
            val uploadLoc = cf.uploadLoc ?: "Unknown"

            files.getOrPut(uploadLoc) { mutableListOf(cf) }.add(cf)

            // Print out how many objects have been processed
            if (System.currentTimeMillis() > nextOutputTime) {
                Log.medium("Processed ${index + 1}/${filesRaw.size} files")
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

        // Download files
        val outDir = SlackTools.folderPath(output)
        for (channelID in files.keys) {
            val channelFolder = outDir.resolve(channelID)
            val filesInChannel = files.getValue(channelID)
            Log.medium("Downloading ${filesInChannel.size} from $channelID")

            for (file in filesInChannel) {
                file.download(channelFolder)
            }
        }
    }
}