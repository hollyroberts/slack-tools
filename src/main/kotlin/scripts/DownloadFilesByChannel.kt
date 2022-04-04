package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import dagger.DaggerWebMainComponent
import json.slack.file.CompleteFile
import json.slack.metadata.ConversationType
import network.http.HttpUtils.ConflictStrategy
import slack.Settings
import slack.downloadByChannel
import slack.filesByConvo
import slack.toCompleteFiles
import java.io.File

fun main(args: Array<String>) = ScriptDownloadByChannel().main(args)

class ScriptDownloadByChannel : CliktCommand(
    name = "download-files-by-channel"
) {
  // Top level options
  @Suppress("unused")
  private val topLevelOptions by TopLevelOptions()
  private val timeOptions by TimeOptions()

  // Auth
  private val token by option("--token", "-t",
      envvar = "SlackToken",
      help = "Authorisation token for slacks web api"
  ).required()

  // Options
  private val user by option("--user", "-u",
      help = "Filters to files only by this user. " +
          "Checks user IDs first, otherwise attempts to resolve the username then display name to ID")
  private val convo by option("--channel", "-c",
      help = "Filters files to those only by this channel. Can be public/private channel or DM. " +
          "Checks channel IDs first, otherwise attempts to resolve the name (with #/@) to ID")

  private val convoTypes by option("--channel-type", "-ct",
      help = "The types of channels to include. Use ',' to separate types. By default all types are included",
      metavar = ConversationType.optionStr())
      .convert { inputStr ->
        inputStr.split(",").map { arg ->
          ConversationType.values().find { arg.lowercase() == it.shortName }
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
    val slack = daggerComponent.getUserAndConvoMap()

    // Resolve user/conversation ID
    val userID = user?.let { slack.inferUserID(it) }
    val convoID = convo?.let { slack.inferChannelID(it) }

    val parsedFiles = daggerComponent.getSlackApi().listFiles(
        startTime = timeOptions.startTime,
        endTime = timeOptions.endTime,
        user = userID,
        channel = convoID
    )
    var completeFiles: Map<String?, List<CompleteFile>> = parsedFiles.toCompleteFiles().filesByConvo()
    if (convoTypes != null) {
      completeFiles = completeFiles.filterKeys { convoTypes!!.contains(slack.conversationType(it)) }
    }
    completeFiles.downloadByChannel(slack, output.toPath())
  }
}