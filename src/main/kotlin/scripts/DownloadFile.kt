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


}