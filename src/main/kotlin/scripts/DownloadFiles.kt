package scripts

import slack.*
import utils.Log
import java.nio.file.Paths
import java.time.Instant

fun main(args: Array<String>) {
    // Basic setup
    Log.mode = Log.Modes.LOW
    val token = args[0]
    val settings = Settings()
    val slack = SlackWebApi(token, settings)

    // TODO
    println(Instant.now().minusSeconds(86_400).epochSecond)

    val parsedFiles = slack.api.getFiles(startTime = Instant.now().minusSeconds(86_400).epochSecond)
    val completeFiles = parsedFiles.toCompleteFiles(slack).filesByConvo()
    completeFiles.downloadFiles(slack, Paths.get("files"), slack.api)

    // slack.downloadFiles(Paths.get("files"))
}