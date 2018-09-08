package slackjson

import java.text.DecimalFormat
import kotlin.math.log
import kotlin.math.max
import kotlin.math.pow

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

    /**
     * Formats the size of the file into a human readable version
     * @param precision Number of decimal places to return (there will be at least 1)
     */
    fun formattedSize(precision: Int) : String {
        if (size < 1024) return "$size B"
        val exp = log(size.toDouble(), 1024.0).toInt()
        val prefix = "KMGTPE"[exp - 1]

        val df = DecimalFormat("#.0" + "#".repeat(max(0, precision - 1)))
        val formattedSize = df.format(size / (1024.0).pow(exp))
        return "$formattedSize ${prefix}iB"
    }
}