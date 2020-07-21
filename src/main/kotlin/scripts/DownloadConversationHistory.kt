package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import dagger.DaggerWebMainComponent
import network.http.HttpUtils.ConflictStrategy
import org.apache.logging.log4j.kotlin.Logging
import slack.Settings
import slackjson.ConversationType
import slackjson.message.TextMessage
import utils.Log
import java.io.File

fun main(args: Array<String>) = ScriptDownloadConversationHistory().main(args)

class ScriptDownloadConversationHistory: CliktCommand(
        name = "download-files-by-channel"
) {
    companion object : Logging

    // Top level options
    private val topLevelOptions by TopLevelOptions()
    private val timeOptions by TimeOptions()

    // Auth
    private val token by option("--token", "-t",
            envvar = "SlackToken",
            help = "Authorisation token for slacks web api"
    ).required()

    private val convo by option("--channel", "-c",
            help = "Download a specific conversations history. Can be public/private channel or DM. " +
                    "Checks channel IDs first, otherwise attempts to resolve the name (with #/@) to ID")

    // TODO support
    private val convoTypes by option("--channel-type", "-ct",
            help = "The types of channels to include. Use ',' to separate types. By default all types are included",
            metavar = ConversationType.optionStr())
            .convert { inputStr ->
                inputStr.split(",").map { arg ->
                    ConversationType.values().find { arg.toLowerCase() == it.shortName }
                            ?: fail("Unknown channel type '$arg'\nAvailable options are: " + ConversationType.optionStr())
                }.toSet()
            }
    private val output by option("--output", "-o",
            help = "Location to output files")
            .file(canBeFile = false)
            .default(File("files"))

    override fun run() {
        // Setup
        val settings = Settings(fileConflictStrategy = ConflictStrategy.HASH).applyTimeOptions(timeOptions)
        val daggerComponent = DaggerWebMainComponent.builder()
                .settings(settings)
                .token(token)
                .build()
        val userAndConvoMap = daggerComponent.getUserAndConvoMap()
        val slack = daggerComponent.getSlackApi()

        val convoId = userAndConvoMap.inferChannelID(convo!!)
        val convoHistory = slack.getConversationHistory(
                userAndConvoMap.conversations[convoId]!!,
                timeOptions.startTime?.toInstant(),
                timeOptions.endTime?.toInstant()
        )

        convoHistory.forEach {
            logger.log(Log.LOW) { (it as TextMessage).text }
        }
        return
    }
}