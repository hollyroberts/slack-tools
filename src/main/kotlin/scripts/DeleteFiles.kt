package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import dagger.DaggerWebMainComponent
import json.slack.file.CompleteFile
import json.slack.file.ParsedFile
import json.slack.metadata.ConversationType
import network.SlackApi
import network.body.FileId
import org.apache.logging.log4j.kotlin.Logging
import slack.Settings
import slack.filesByConvo
import slack.toCompleteFiles
import utils.Log
import utils.formatSize

fun main(args: Array<String>) = ScriptDeleteFiles().main(args)

class ScriptDeleteFiles : CliktCommand(
    name = "delete-files"
) {
  companion object: Logging

  // Top level options
  @Suppress("unused")
  private val topLevelOptions by TopLevelOptions()
  private val timeOptions by TimeOptions()

  // Auth
  private val token by option("--token", "-t",
      envvar = "SlackToken",
      help = "Authorisation token for slacks web api"
  ).required()

  // Required options
  private val dryRun: Boolean by option("--dryrun", "-dr",
  help = "Whether to skip deletion")
      .convert { !it.equals("false", ignoreCase = false) }
      .default(true)

  // Options
  private val user: String? by option("--user", "-u",
      help = "Filters to files only by this user. " +
          "Checks user IDs first, otherwise attempts to resolve the username then display name to ID")
  private val convo: String? by option("--channel", "-c",
      help = "Filters files to those only by this channel. Can be public/private channel or DM. " +
          "Checks channel IDs first, otherwise attempts to resolve the name (with #/@) to ID")

  private val convoTypes: Set<ConversationType>? by option("--channel-type", "-ct",
      help = "The types of channels to include. Use ',' to separate types. By default all types are included",
      metavar = ConversationType.optionStr())
      .convert { inputStr ->
        inputStr.split(",").map { arg ->
          ConversationType.values().find { arg.lowercase() == it.shortName }
              ?: fail("Unknown channel type '$arg'\nAvailable options are: " + ConversationType.optionStr())
        }.toSet()
      }

  override fun run() {
    logger.log(Log.HIGH) { "Dry run enabled: $dryRun" }

    // Setup
    val settings = Settings().applyTimeOptions(timeOptions)
    val daggerComponent = DaggerWebMainComponent.builder()
        .settings(settings)
        .token(token)
        .build()
    val slack = daggerComponent.getUserAndConvoMap()

    // Resolve user/conversation ID
    val userID = user?.let { slack.inferUserID(it) }
    val convoID = convo?.let { slack.inferChannelID(it) }

    val api: SlackApi = daggerComponent.getSlackApi()
    val parsedFiles: List<ParsedFile> = api.listFiles(
        startTime = timeOptions.startTime,
        endTime = timeOptions.endTime,
        user = userID,
        channel = convoID
    )

    val fileConvoMap: Map<String?, List<CompleteFile>> = parsedFiles.toCompleteFiles().filesByConvo()
        .filterKeys { convoTypes == null || convoTypes!!.contains(slack.conversationType(it)) }
        .mapKeys { slack.conversationName(it.key) }
    val totalFiles: Int = fileConvoMap.asSequence()
        .flatMap { it.value }
        .count()
    val totalSize: Long = fileConvoMap.asSequence()
        .flatMap { it.value }
        .sumOf { it.size }

    // Store values as we process instead of pre-computed values as we might be in a dry run
    var processedFiles: Int = 0
    var processedSize: Long = 0

    logger.log(Log.HIGH) { "Found %,d files to delete. Total size: %s".format(totalFiles, formatSize(totalSize)) }
    fileConvoMap.asSequence()
        .sortedBy { it.key }
        .forEach { entry ->
          val files = entry.value
          logger.info { "Processing %s. Files to delete: %,d (%s)".format(entry.key, files.size, formatSize(files.sumOf { it.size })) }
          files.forEach {
            val fileInfo = "'${it.title}' (${formatSize(it.size)}) by ${slack.userUsername(it.user)}"

            if (dryRun) {
              logger.log(Log.LOW) { "Would delete $fileInfo"}
            } else {
              logger.log (Log.LOW) { "Deleting file $fileInfo" }
              api.deleteFile(FileId(it.id))
              processedFiles++
              processedSize += it.size
            }
          }
        }

    logger.log(Log.HIGH) { "Deleted %,d files. Total size: %s".format(processedFiles, formatSize(processedSize)) }
  }
}