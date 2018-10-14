import utils.Log
import slack.SlackDataFromApi
import slack.Settings
import java.nio.file.Paths

fun main(args: Array<String>) {
    // Basic setup
    val token = args[0]
    val outDir = Paths.get("files")
    val settings = Settings()
    val slack = SlackDataFromApi(token, settings)

    // Process conversations alphabetically
    Log.high("Downloading files")
    slack.filesByConvo.keys.sortedBy { slack.getConversationName(it) }.forEach { convoID ->
        val filesInConvo = slack.filesByConvo[convoID]!!

        // Get location to put files in
        val convoName = slack.getConversationName(convoID)
        val convoFolder = outDir.resolve(convoName)

        // Create folder if it doesn't exist
        if (!convoFolder.toFile().exists()) {
            Log.debugHigh("Creating directory ' ${convoFolder.fileName}'")
            convoFolder.toFile().mkdir()
        }

        // Download files
        // TODO track successes/failures
        Log.medium("Downloading ${filesInConvo.size} files from $convoName")
        filesInConvo.sortedBy { it.timestamp }.forEach { file ->
            file.download(convoFolder, slack)
        }
    }
}