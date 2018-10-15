package slackjson

import slack.SlackData
import utils.Api
import utils.Http
import utils.Log
import java.io.File
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class CompleteFile(sf: SlackFile, private val api: Api, infer: Boolean = true) : SlackFile {
    override val id = sf.id
    override val user = sf.user
    override val title = sf.title

    override val filetype = sf.filetype
    override val size = sf.size
    override val timestamp = sf.timestamp
    override val urlPrivate = sf.urlPrivate
    override val urlPrivateDownload = sf.urlPrivateDownload

    // Lists can be copied by reference as they're not mutable (map is also immutable)
    override val channels = sf.channels
    override val groups = sf.groups
    override val ims = sf.ims

    val uploadLoc = if (infer) {
        when {
            channelsUploadedIn() == 1 -> channels?.firstOrNull() ?: groups?.firstOrNull() ?: ims!![0]
            channelsUploadedIn() == 0 -> {
                Log.warn("File $id belongs to no channels")
                null
            }
            else -> {
                Log.debugHigh("File $id belongs to more than one channel, requires API call to resolve")
                resolveMultipleLocations(this)
            }
        }
    } else {
        resolveMultipleLocations(this)
    }

    fun download(folder: Path, slack: SlackData, http: Http) : DownloadStatus {
        // Assemble file name
        val datetime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("UTC"))
        var formattedName = "[${dtf.format(datetime)}] - ${slack.getUsername(user)} - $title"

        // Strip out/replace illegal chars
        formattedName = Regex("""[/*?"<>|]""").replace(formattedName, "")
        formattedName =  formattedName.replace(":", ";")

        // Add extension if it doesn't exist
        if (File(formattedName).extension.isEmpty() && filetype.isNotEmpty()) {
            formattedName += ".$filetype"
        }

        // Download
        urlPrivateDownload?.let {
            return http.downloadFile(it, folder.resolve(formattedName), size, slack.settings.inferFileLocation)
        } ?: urlPrivate.let {
            Log.low("File $id does not have the property url_private_download. Saving external link to '$formattedName'")
            folder.resolve("$formattedName.txt").toFile().writeText("Link: $it")
            return DownloadStatus.LINK
        }
    }

    /**
     * Parsed files can have multiple channels, we want to figure out where it was shared to first
     * Used during initialisation
     * Returns id of channel that file was first seen in
     */
    private fun resolveMultipleLocations(cf: CompleteFile) : String? {
        val pf = api.getFile(cf.id)
        return pf.inferLocFromShares()
    }

    companion object {
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd - HH;mm")!!
    }
}

enum class DownloadStatus { SUCCESS, SUCCESS_OVERWRITE, ALREADY_EXISTED, LINK, FAILURE }