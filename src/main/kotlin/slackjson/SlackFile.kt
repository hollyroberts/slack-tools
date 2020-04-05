package slackjson

import slack.SlackData
import slackjson.SlackFile.FormattingType.Companion.defaultType
import utils.DownloadStatus
import utils.Http
import utils.Log
import utils.WebApi
import java.io.File
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@MoshiInject
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

    fun channelsUploadedIn() = (channels?.size ?: 0) + (ims?.size ?: 0) + (groups?.size ?: 0)

    fun download(folder: Path, slack: SlackData, webApi: WebApi?, formatting: FormattingType? = null) : DownloadStatus {
        // Strip out/replace illegal chars
        var formattedName = formattedDownloadName(formatting, slack)
        formattedName = Regex("""[/*?"<>|]""").replace(formattedName, "")
        formattedName =  formattedName.replace(":", ";")

        // Add extension if it doesn't exist
        if (File(formattedName).extension.isEmpty() && filetype.isNotEmpty()) {
            formattedName += ".$filetype"
        }

        // Download
        urlPrivateDownload?.let {
            return webApi?.downloadFile(it, folder.resolve(formattedName), size, slack.settings.fileConflictStrategy)
                    ?: run {
                        Http().downloadFile(it, folder.resolve(formattedName), size, slack.settings.fileConflictStrategy)
                    }
        } ?: urlPrivate.let {
            Log.low("File $id does not have the property 'url_private_download'. Saving external link to '$formattedName'")
            folder.resolve("$formattedName.txt").toFile().writeText("Link: $it")
            return DownloadStatus.LINK
        }
    }

    private fun formattedDownloadName(type: FormattingType?, slack: SlackData) : String {
        // Calculate intermediate strings
        val username = if (slack.settings.useDisplayNamesForFiles) {
            slack.userDisplayname(user)
        } else {
            slack.userUsername(user)
        }
        val datetime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), slack.settings.outTz)

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

    companion object {
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd - HH;mm")!!
    }
}