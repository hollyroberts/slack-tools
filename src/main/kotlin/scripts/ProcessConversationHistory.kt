package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import dagger.DaggerExportMainComponent
import network.http.HttpUtils.ConflictStrategy
import org.apache.logging.log4j.kotlin.Logging
import slack.Settings
import slackjson.ConversationType
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

        val dagger = DaggerExportMainComponent.builder()
                .settings(settings)
                .folderLocation(input.toPath())
                .build()
        val userAndConvoMap = dagger.getUserAndConvoMap()
        val exportProcessor = dagger.getExportProcessor()


        userAndConvoMap.conversations.values
                .sortedBy { it.nameRaw() }
                .forEach {
                    exportProcessor.loadConversationFolder(it)
                }

        // TODO replace this with filtering
        // val firstConvo = userAndConvoMap.conversations.values.sortedBy { it.nameRaw() }[0]
        // exportProcessor.loadConversationFolder(firstConvo)
    }
}