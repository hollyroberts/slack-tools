import utils.Log
import slack.SlackDataFromApi
import slack.Settings
import slackjson.DownloadStatus
import utils.ensureFolderExists
import java.nio.file.Paths

fun main(args: Array<String>) {
    // Basic setup
    val token = args[0]
    val outDir = Paths.get("files")
    val settings = Settings()
    val slack = SlackDataFromApi(token, settings)

    // Process conversations alphabetically
    Log.high("Downloading files")
    var downloadStats = DownloadStats()
    slack.filesByConvo.keys.sortedBy { slack.getConversationName(it) }.forEach { convoID ->
        val filesInConvo = slack.filesByConvo[convoID]!!

        // Get location to put files in
        val convoName = slack.getConversationName(convoID)
        val convoFolder = outDir.resolve(convoName)

        // Create folder if it doesn't exist
        ensureFolderExists(convoFolder)

        // Download files
        val channelStats = DownloadStats()
        Log.medium("Downloading ${filesInConvo.size} files from $convoName")
        filesInConvo.sortedBy { it.timestamp }.forEach { file ->
            channelStats.update(file.download(convoFolder, slack))
        }

        channelStats.log(convoName)
        downloadStats += channelStats
    }

    downloadStats.log("slack")
}

/**
 * Could probably use a map for this, oh well now
 */
@Suppress("MemberVisibilityCanBePrivate")
class DownloadStats {
    private val map = mutableMapOf<DownloadStatus, Int>()

    init {
        DownloadStatus.values().forEach {
            map[it] = 0
        }
    }

    // Functions for collecting data
    fun update(status: DownloadStatus) {
        map[status] = map[status]!! + 1
    }

    operator fun plus(other: DownloadStats) : DownloadStats {
        val new = DownloadStats()

        DownloadStatus.values().forEach {
            new.map[it] = map[it]!! + other.map[it]!!
        }

        return new
    }

    // Data retrieval methods
    fun links() = map[DownloadStatus.LINK]!!

    fun successes() = map[DownloadStatus.SUCCESS]!! +
                      map[DownloadStatus.SUCCESS_OVERWRITE]!! +
                      map[DownloadStatus.ALREADY_EXISTED]!! +
                      links()

    fun failures() = map[DownloadStatus.FAILURE]!!

    fun total() = successes() + failures()

    fun getMessage(location: String) : String {
        var msg = "Downloaded ${successes()}/${total()} files successfully from $location"

        if (links() > 0) {
            msg += " (of which ${links()} were links and saved to text files)"
        }

        return msg
    }

    fun log(location: String) {
        // Output information
        if (failures() == 0) {
            Log.medium(getMessage(location))
        } else {
            Log.warn(getMessage(location))
        }
    }
}