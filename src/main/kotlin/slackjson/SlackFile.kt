package slackjson

interface SlackFile {
    // Identification
    val id: String
    val user: String
    val title: String

    // Metadata
    val filetype: String
    val size: Long
    val timestamp: Long
    val urlPrivate: String
    val urlPrivateDownload: String?

    // Where has this file been sent
    // Won't be included if file object is directly from a channel
    val channels: List<String>?
    val groups: List<String>?
    val ims: List<String>?

    fun channelsUploadedIn() = (channels?.size ?: 0) + (ims?.size ?: 0) + (groups?.size ?: 0)
}