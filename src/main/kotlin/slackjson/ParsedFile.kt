package slackjson

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
open class ParsedFile (
        // Identification
        override val id: String,
        override val user: String,
        override val title: String,

        // Metadata
        override val filetype: String,
        override val size: Long,
        override val url_private_download: String,

        // Where has this file been sent
        // Won't be included if file object is directly from a channel
        override val channels: List<String>?,
        override val groups: List<String>?,
        override val ims: List<String>?,

        override val shares: FileShare?
) : SlackFile {
    var firstSeen = mutableMapOf<String, Double>()

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