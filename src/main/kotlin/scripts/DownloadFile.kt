import utils.Log
import slack.SlackDataFromApi
import java.nio.file.Paths

fun main(args: Array<String>) {
    // Basic setup
    val token = args[0]
    val outDir = Paths.get("files")
    val slack = SlackDataFromApi(token)

    for ((convoID, filesInConvo) in slack.filesByConvo) {
        // Get location to put files in
        val convoName = if (convoID != null) {
            val convo = slack.conversations[convoID]!!
            convo.getFullName()
        } else {
            "Unknown location"
        }
        val convoFolder = outDir.resolve(convoName)

        // Download files
        // TODO track successes
        Log.high("Downloading files")
        Log.medium("Downloading ${filesInConvo.size} files from $convoName")
        for (file in filesInConvo) {
            file.download(convoFolder, slack)
        }
    }
}