package scripts

import slack.Settings
import slack.SlackWebApi
import java.nio.file.Paths

fun main(args: Array<String>) {
    // Basic setup
    val token = args[0]
    val settings = Settings()
    val slack = SlackWebApi(token, settings)

    // TODO
    // slack.downloadFiles(Paths.get("files"))
}