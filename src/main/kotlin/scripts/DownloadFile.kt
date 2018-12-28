import slack.SlackDataFromApi
import slack.Settings
import java.nio.file.Paths

fun main(args: Array<String>) {
    // Basic setup
    val token = args[0]
    val settings = Settings()
    val slack = SlackDataFromApi(token, settings)

    // slack.downloadFiles(Paths.get("files"))
    slack.downloadAvatars(Paths.get("avatars"))
}