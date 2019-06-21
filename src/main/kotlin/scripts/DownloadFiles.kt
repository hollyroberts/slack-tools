package scripts

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import slack.*
import slackjson.ConversationTypes
import utils.Log
import java.nio.file.Paths
import java.time.Instant

fun main(args: Array<String>) {
    // Basic setup
    Log.mode = Log.Modes.DEBUG_LOW
    val token = args[0]
    val settings = Settings()
    val slack = SlackWebApi(token, settings)

    println(slack.conversations.keys.map { slack.conversationName(it) })
    println(slack.conversations.count { it.value.conversationType() == ConversationTypes.PUBLIC_CHANNEL})

    // TODO
    println(Instant.now().minusSeconds(86_400).epochSecond)

    val parsedFiles = slack.api.getFiles(startTime = Instant.now().minusSeconds(86_400).epochSecond)
    val completeFiles = parsedFiles.toCompleteFiles(slack).filesByConvo()
    completeFiles.downloadFiles(slack, Paths.get("files"), slack.api)
}

open class TopLevelArgs : OptionGroup(
        name="Top level arguments",
        help="") {

    val logMode: Log.Modes? by option("--log-mode").convert {
        Log.argStringMap()[it.toUpperCase()] ?: run {
            val options = Log.argStringMap().keys
            fail("Unknown log level '$it'. Available options are:\n" + options.joinToString("\n\t"))
        }

    }
}