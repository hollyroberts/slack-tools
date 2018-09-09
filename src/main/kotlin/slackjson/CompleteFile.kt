package slackjson

import utils.Api
import utils.Http
import utils.Log
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter




class CompleteFile(sf: SlackFile, infer: Boolean = true) : SlackFile {
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

    fun download(folder: Path) {
        // Create name
        val datetime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("UTC"))
        val formattedName = "[${dtf.format(datetime)}] - $title"

        // Download
        urlPrivateDownload?.let {
            Log.medium("Downloading: '$it' (${formattedSize(2)})")
            Http.downloadFile(it, folder.resolve(formattedName))
        } ?: urlPrivate.let {
            Log.medium("File $id does not have the property url_private_download. Saving external link to text document.")
        }
    }

    /**
     * Parsed files can have multiple channels, we want to figure out where it was shared to first
     * Used during initialisation
     * Returns id of channel that file was first seen in
     */
    private fun resolveMultipleLocations(cf: CompleteFile) : String? {
        val pf = Api.getFile(cf.id)
        return pf.inferLocFromShares()
    }

    companion object {
        val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH;mm")!!
    }
}