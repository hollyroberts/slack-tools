import utils.Log
import slack.SlackDataFromApi
import java.nio.file.Paths

fun main(args: Array<String>) {
    // Basic setup
    val token = args[0]
    val outDir = Paths.get("files")
    val slack = SlackDataFromApi(token)

    for (channelID in files.keys) {
        val channelFolder = outDir.resolve(channelID)
        val filesInChannel = files.getValue(channelID)
        Log.medium("Downloading ${filesInChannel.size} from $channelID")

        for (file in filesInChannel) {
            file.download(channelFolder)
        }
    }
}