package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import dagger.DaggerWebMainComponent
import network.http.HttpUtils.ConflictStrategy
import org.apache.logging.log4j.kotlin.Logging
import slack.Settings
import slack.downloadByUser
import slack.filesByUser
import java.io.File

fun main(args: Array<String>) = ScriptDownloadByUser().main(args)

class ScriptDownloadByUser : CliktCommand(
    name = "download-files-by-user"
) {
  companion object : Logging

  // Top level options
  @Suppress("unused")
  private val topLevelOptions by TopLevelOptions()
  private val timeOptions by TimeOptions()

  // Auth
  private val token: String by option("--token", "-t",
      envvar = "SlackToken",
      help = "Authorisation token for slacks web api"
  ).required()

  // Options
  private val convo by argument(name = "channel",
      help = "Filters files to those only by this channel. Can be public/private channel or DM. " +
          "Checks channel IDs first, otherwise attempts to resolve the name (with #/@) to ID")
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
    val convoID = convo.let { slack.inferChannelID(it) } ?: run {
      logger.error { "Could not infer channel from '$convo'" }
      return
    }

    val parsedFiles = daggerComponent.getSlackApi().listFiles(
        startTime = timeOptions.startTime,
        endTime = timeOptions.endTime,
        channel = convoID
    )

    val filesByUser = parsedFiles.filesByUser()
    filesByUser.downloadByUser(slack, output.toPath())
  }
}