package slackjson

import dagger.Lazy
import network.http.HttpUtils
import org.apache.logging.log4j.kotlin.Logging
import slack.Settings
import slack.SlackData
import slackjson.SlackFile.FormattingType.Companion.defaultType
import utils.DownloadStatus
import utils.Log
import java.io.File
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

abstract class SlackFile : BaseFile() {
    // Identification
    abstract val id: String
    abstract val user: String
    abstract val title: String

    // Metadata
    abstract val filetype: String
    abstract val size: Long
    abstract val timestamp: Long
    abstract val urlPrivate: String
    abstract val urlPrivateDownload: String?

    // Where has this file been sent
    // Won't be included if file object is directly from a channel
    abstract val channels: List<String>?
    abstract val groups: List<String>?
    abstract val ims: List<String>?

    // To be injected at some point
    abstract var slackData: Lazy<SlackData>
    abstract var settings: Settings
    abstract var httpUtils: HttpUtils

    companion object : Logging {
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd - HH;mm")!!
    }

    fun channelsUploadedIn() = (channels?.size ?: 0) + (ims?.size ?: 0) + (groups?.size ?: 0)

    fun download(folder: Path, formatting: FormattingType? = null) : DownloadStatus {
        // Strip out/replace illegal chars
        var formattedName = formattedDownloadName(formatting)
        formattedName = Regex("""[/*?"<>|]""").replace(formattedName, "")
        formattedName =  formattedName.replace(":", ";")

        // Add extension if it doesn't exist
        if (File(formattedName).extension.isEmpty() && filetype.isNotEmpty()) {
            formattedName += ".$filetype"
        }

        // Download
        return if (urlPrivateDownload != null) {
            httpUtils.downloadFile(urlPrivateDownload!!, folder.resolve(formattedName), size, settings.fileConflictStrategy)
        } else {
            logger.log(Log.LOW) { "File $id does not have the property 'url_private_download'. Saving external link to '$formattedName'" }
            folder.resolve("$formattedName.txt").toFile().writeText("Link: $urlPrivate")
            DownloadStatus.LINK
        }
    }

    internal fun formattedDownloadName(type: FormattingType?) : String {
        // Calculate intermediate strings
        val username = if (settings.useDisplayNamesForFiles) {
            slackData.get().userDisplayname(user)
        } else {
            slackData.get().userUsername(user)
        }
        val datetime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), settings.outTz)

        return when(type ?: defaultType()) {
            FormattingType.STANDARD -> "[${dtf.format(datetime)}] - $username - $title"
            FormattingType.WITHOUT_NAME -> "[${dtf.format(datetime)}] - $title"
        }
    }

    enum class FormattingType {
        STANDARD,
        WITHOUT_NAME;

        companion object {
            fun defaultType() = STANDARD
        }
    }
}