import com.squareup.moshi.JsonClass
import java.text.DecimalFormat
import kotlin.math.log
import kotlin.math.max
import kotlin.math.pow

@JsonClass(generateAdapter = true)
data class File(
        // Identification
        val id: String,
        val user: String,
        val title: String,

        // Metadata
        val filetype: String,
        val size: Long,
        val url_private_download: String,

        // Where has this file been sent
        // Won't be included if file object is directly from a channel
        val channels: List<String>?,
        val ims: List<String>,

        @Transient
        val shareTime: Map<String, Double> = mutableMapOf()
) {
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

    /**
     * Depending on how they were retrieved files might not have the full set of data that we're interested in
     * Right now this is the channel that the file was uploaded in
     * This is added either manually (if it can be inferred from the channel history parser), or by this method which uses the slack api (so much slower)
     */
    fun retrieveIncompleteData() {

    }
}