package slack

import slackjson.CompleteFile
import utils.DownloadStats
import utils.Log
import utils.WebApi
import utils.ensureFolderExists
import java.nio.file.Path

object FileList {
    fun downloadFiles(slack: SlackData, outDir: Path, filesByConvo: Map<String?, MutableList<CompleteFile>>, webApi: WebApi?) {
        // Process conversations alphabetically
        Log.high("Downloading files")
        var downloadStats = DownloadStats()
        filesByConvo.keys.sortedBy { slack.getConversationName(it) }.forEach { convoID ->
            val filesInConvo = filesByConvo[convoID]!!

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
}