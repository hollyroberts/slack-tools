import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import java.text.DecimalFormat
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
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
        val groups: List<String>?,
        val ims: List<String>?,

        val shares: FileShare?
) {
    var firstSeen: MutableMap<String, Double>? = null
    var uploadLocation: String? = null
    init {
        // Destruct FileShare if it exists
        firstSeen = shares?.firstSeen

        // Update uploadLocation
        if (channelsUploadedIn() == 1) {
            uploadLocation = channels?.get(0) ?: groups?.get(0) ?: ims!![0]
        } else {
            updateUploadChannelFromShares(shares)
        }
    }

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

    private fun channelsUploadedIn() = (channels?.size ?: 0) + (ims?.size ?: 0) + (groups?.size ?: 0)

    /**
     * Depending on how they were retrieved files might not have the full set of data that we're interested in
     * Right now this is the channel that the file was uploaded in
     * This is added either manually (if it can be inferred from the channel history parser), or by this method which uses the slack api (so much slower)
     */
    fun retrieveIncompleteData() {
        if (uploadLocation != null) {
            return
        }

        val f = Api.getFile(id)
        firstSeen = f.shares!!.firstSeen
        updateUploadChannelFromShares(shares)
    }

    fun addShareLocation(location: String, timestamp: Double) {
        if (firstSeen == null) {
            firstSeen = mutableMapOf(Pair(location, timestamp))
            return
        }

        if (!firstSeen!!.containsKey(location)) {
            firstSeen!![location] = timestamp
        } else {
            if (firstSeen!![location]!! > timestamp) {
                firstSeen!![location] = timestamp
            }
        }
    }

    /**
     * It's possible to add shares manually (eg. when traversing history), so this should be called at the end
     */
    fun updateUploadChannelFromShares(shares: FileShare?) {
        if (shares?.firstSeen?.isNotEmpty() == true) {
            uploadLocation = shares.firstSeen.minBy { it.value }!!.key
        }
    }
}

object ShareJsonAdapter {
    @FromJson fun extractShareInfo(reader: JsonReader) : FileShare {
        val map = mutableMapOf<String, Double>()

        reader.beginObject()
        // Iterate over public/private
        while (reader.peek() != JsonReader.Token.END_OBJECT) {
            val channelType = reader.nextName()

            if (channelType != "public" && channelType != "private") {
                throw JsonDataException("File share data was not public or private (was $channelType)")
            }

            // Iterate over channel IDs
            reader.beginObject()
            while (reader.peek() != JsonReader.Token.END_OBJECT) {
                val (id, timestamp) = processChannel(reader)
                map[id] = timestamp
            }
            reader.endObject()
        }
        reader.endObject()

        return FileShare(map)
    }

    private fun processChannel(reader: JsonReader) : Pair<String, Double> {
        val id = reader.nextName()
        var lowestTs = Double.MAX_VALUE
        reader.beginArray()

        // Iterate over array
        while (reader.peek() != JsonReader.Token.END_ARRAY) {
            // Iterate over keys
            reader.beginObject()
            while (reader.peek() != JsonReader.Token.END_OBJECT) {
                if (reader.nextName() != "ts") {
                    reader.skipValue()
                } else {
                    lowestTs = min(lowestTs, reader.nextDouble())
                }
            }
            reader.endObject()
        }
        reader.endArray()

        return Pair(id, lowestTs)
    }
}

data class FileShare(
        val firstSeen: MutableMap<String, Double>
)