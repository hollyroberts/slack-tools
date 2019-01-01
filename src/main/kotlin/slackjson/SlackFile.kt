package slackjson

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
}