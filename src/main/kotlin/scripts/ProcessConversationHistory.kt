package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import dagger.DaggerExportMainComponent
import json.slack.metadata.Conversation
import json.slack.metadata.ConversationType
import network.http.HttpUtils.ConflictStrategy
import org.apache.logging.log4j.kotlin.Logging
import slack.Settings
import utils.Log
import utils.PerformanceLogging
import java.io.File
import java.nio.file.Paths

fun main(args: Array<String>) = ScriptProcessConversationHistory().main(args)

class ScriptProcessConversationHistory : CliktCommand(
        name = "download-files-by-channel"
) {
    companion object : Logging

    // Top level options
    private val topLevelOptions by TopLevelOptions()
    private val timeOptions by TimeOptions()

    private val input by option("--input", "-i",
            help = "Location of slack export")
            .file(canBeFile = false, mustExist = true, mustBeReadable = true)
            .default(Paths.get("").toFile())

    private val convo by option("--channel", "-c",
            help = "Download a specific conversations history. Can be public/private channel or DM. " +
                    "Checks channel IDs first, otherwise attempts to resolve the name (with #/@) to ID")

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
            .file(canBeFile = false, mustExist = true)
            .default(File("files"))

    override fun run() {
        // Setup
        val settings = Settings(fileConflictStrategy = ConflictStrategy.HASH).applyTimeOptions(timeOptions)

        val dagger = DaggerExportMainComponent.builder()
                .settings(settings)
                .folderLocation(input.toPath())
                .build()
        val userAndConvoMap = dagger.getUserAndConvoMap()
        val exportProcessor = dagger.getExportProcessor()

        // TODO make this less janky?
        var convos: List<Conversation>
        if (convo == null) {
            convos = userAndConvoMap.conversations
                    .values
                    .sortedBy { it.nameRaw() }
        } else {
            val convoId = userAndConvoMap.inferChannelID(convo!!) ?: run {
                logger.error { "Could not infer channel from '$convo'" }
                return
            }
            convos = listOf(userAndConvoMap.conversations[convoId]!!)
        }

        if (convoTypes != null) {
            convos = convos.filter { convoTypes!!.contains(it.type) }
        }
        if (convos.isEmpty()) {
            logger.error("Conversations to download is empty after filtering")
            return
        }

        logger.log(Log.HIGH) { "Loading conversation data" }


        // Load from disk
        val loadStats = convos.map {
            exportProcessor.loadConversationFolder(it)
        }

        // Log total messages/dropped
        val droppedMessages = loadStats.sumBy { it.messagesDropped }
        val loadedMessages = loadStats.sumBy { it.messagesLoaded }
        val droppedPercentage = (100 * droppedMessages.toFloat()) / loadedMessages

        if (droppedMessages == 0) {
            logger.log(Log.HIGH) { String.format("Loaded %,d messages", loadedMessages) }
        } else {
            logger.log(Log.HIGH) { String.format("Loaded %,d messages. Dropped messages: %,d (%.2f%%)", loadedMessages, droppedMessages, droppedPercentage) }
        }

        // Log subtype breakdown
        val typeRecorder = dagger.getMessageTypeRecorder()
        typeRecorder.logResults()
        PerformanceLogging.outputMemoryUsage()
    }
}