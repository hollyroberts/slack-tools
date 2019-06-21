package slackjson

import slack.SlackData
import utils.DownloadStatus
import utils.Http
import utils.Log
import utils.WebApi
import java.io.File
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class CompleteFile(sf: SlackFile, val uploadLoc: String?) : SlackFile() {
    override val id = sf.id
    override val user = sf.user
    override val title = sf.title

    override val mode = sf.mode
    override val filetype = sf.filetype
    override val size = sf.size
    override val timestamp = sf.timestamp
    override val urlPrivate = sf.urlPrivate
    override val urlPrivateDownload = sf.urlPrivateDownload

    // Lists can be copied by reference as they're not mutable (map is also immutable)
    override val channels = sf.channels
    override val groups = sf.groups
    override val ims = sf.ims

    fun download(folder: Path, slack: SlackData, webApi: WebApi?) : DownloadStatus {
        // Assemble file name
        val username = if (slack.settings.useDisplayNamesForFiles) {
            slack.userDisplayname(user)
        } else {
            slack.userUsername(user)
        }
        val datetime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("UTC"))
        var formattedName = "[${dtf.format(datetime)}] - $username - $title"

        // Strip out/replace illegal chars
        formattedName = Regex("""[/*?"<>|]""").replace(formattedName, "")
        formattedName =  formattedName.replace(":", ";")

        // Add extension if it doesn't exist
        if (File(formattedName).extension.isEmpty() && filetype.isNotEmpty()) {
            formattedName += ".$filetype"
        }

        // Download
        urlPrivateDownload?.let {
            return webApi?.downloadFile(it, folder.resolve(formattedName), size, slack.settings.ignoreDownloadedFiles)
                    ?: run {
                        Http().downloadFile(it, folder.resolve(formattedName), size, slack.settings.ignoreDownloadedFiles)
                    }
        } ?: urlPrivate.let {
            Log.low("File $id does not have the property 'url_private_download'. Saving external link to '$formattedName'")
            folder.resolve("$formattedName.txt").toFile().writeText("Link: $it")
            return DownloadStatus.LINK
        }
    }

    companion object {
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd - HH;mm")!!
    }
}