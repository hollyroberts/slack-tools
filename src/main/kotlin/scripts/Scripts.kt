package scripts

import slack.SlackWebApi
import slack.Settings
import slack.Avatars
import utils.Log
import java.nio.file.Paths

fun main(args: Array<String>) {
    Scripts.downloadAvatars(args)
}

object Scripts {
    fun downloadAvatars(args: Array<String>) {
        // Basic setup
        Log.mode = Log.Modes.LOW
        val token = args[0]
        val settings = Settings()
        val slack = SlackWebApi(token, settings)

        // TODO
        Avatars(slack).downloadAvatars(Paths.get("avatars"))
    }
}