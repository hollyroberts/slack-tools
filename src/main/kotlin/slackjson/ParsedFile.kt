package slackjson

import com.squareup.moshi.*
import kotlin.math.min

@JsonClass(generateAdapter = true)
open class ParsedFile (
        // Identification
        override val id: String,
        override val user: String,
        override val title: String,

        // Metadata
        override val filetype: String,
        override val size: Long,
        @Json(name = "created")
        override val timestamp: Long,
        @Json(name = "url_private")
        override val urlPrivate: String,
        @Json(name = "url_private_download")
        override val urlPrivateDownload: String?,

        // Where has this file been sent
        // Won't be included if file object is directly from a channel
        override val channels: List<String>?,
        override val groups: List<String>?,
        override val ims: List<String>?,

        // Non inherited properties
        val shares: FileShare?
) : SlackFile {
    @Transient
    private val custTimestamps = mutableMapOf<String, Double>()

    /**
     * Manually add a timestamp record of when a file was first seen in a channel
     * Will not update if this is later than the earliest file
     */
    fun addLocationTimestamp(location: String, timestamp: Double) {
        if (custTimestamps.containsKey(location)) {
            if (custTimestamps.getValue(location) > timestamp) {
                custTimestamps[location] = timestamp
            }
        } else {
            custTimestamps[location] = timestamp
        }
    }

    /**
     * Infers the share location based on the timestamps provided by addLocationTimestamp
     */
    fun inferLocFromTimestamps() = custTimestamps.minBy { it.value }?.key

    /**
     * If API call returned shared data then this is the most accurate way to infer where the file was uploaded
     */
    fun inferLocFromShares() = shares?.firstSeen?.minBy { it.value }?.key
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
        val firstSeen: Map<String, Double>
)