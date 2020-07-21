package slackjson

import com.squareup.moshi.*
import dagger.Lazy
import network.SlackApi
import network.http.HttpUtils
import org.apache.logging.log4j.kotlin.Logging
import slack.Settings
import slack.SlackData
import utils.iterateArray
import utils.iterateObject
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@MoshiInject
@JsonClass(generateAdapter = true)
open class ParsedFile (
        // Identification
        override val id: String,
        override val user: String,
        override val title: String,

        // Metadata
        override val mode: String,
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
        @Suppress("MemberVisibilityCanBePrivate")
        val shares: FileShare?
) : SlackFile() {
    @Transient
    @Inject
    override lateinit var settings: Settings

    @Transient
    @Inject
    override lateinit var slackData: Lazy<SlackData>

    @Transient
    @Inject
    lateinit var api: Optional<SlackApi>

    @Transient
    @Inject
    override lateinit var httpUtils: HttpUtils

    @Transient
    private val custTimestamps = mutableMapOf<String, BigDecimal>()

    companion object : Logging

    /**
     * Manually add a timestamp record of when a file was first seen in a channel
     * Will not update if this is later than the earliest file
     */
    fun addLocationTimestamp(location: String, timestamp: BigDecimal) {
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
     * Returns null if no timestamps were given
     */
    private fun inferLocFromTimestamps() = custTimestamps.minBy { it.value }?.key

    /**
     * If API call returned shared data then this is the most accurate way to infer where the file was uploaded
     * However files.list does not return this data, only files.info
     */
    private fun inferLocFromShares() = shares?.firstSeen?.minBy { it.value }?.key

    /**
     * Gets the exact upload location by querying a single file
     */
    private fun getLocationFromApi() = api.get()
            .listFileInfo(this.id)
            .inferLocFromShares()

    /**
     * Returns a new complete file instance
     * The upload location is based upon the earliest timestamp given
     */
    fun toCompleteFileFromTimestamps() = CompleteFile(this, inferLocFromTimestamps())

    /**
     * Returns a new complete file instance
     * Infers location based on share locations. This could be inaccurate if the message was deleted but not the file and then shared elsewhere
     * However in most cases we wouldn't be able to figure out it's location anyway
     */
    fun toCompleteFileByInference() : CompleteFile {
        val location = inferLocFromShares() ?: when (channelsUploadedIn()) {
            1 -> channels?.firstOrNull() ?: groups?.firstOrNull() ?: ims!![0]
            0 -> {
                logger.warn { "File $id belongs to no channels" }
                null
            } else -> {
                logger.debug { "File $id belongs to more than one channel, requires API call to resolve" }
                getLocationFromApi()
            }
        }

        return CompleteFile(this, location)
    }
}

data class FileShare(
        val firstSeen: Map<String, BigDecimal>
)

object ShareJsonAdapter {
    @FromJson
    fun extractShareInfo(reader: JsonReader) : FileShare {
        val map = mutableMapOf<String, BigDecimal>()

        reader.iterateObject {
            // Iterate over public/private
            val channelType = reader.nextName()

            if (channelType != "public" && channelType != "private") {
                throw JsonDataException("File share data was not public or private (was $channelType)")
            }

            // Iterate over channel IDs
            reader.iterateObject {
                val (id, timestamp) = processChannel(reader)
                map[id] = timestamp
            }
        }

        return FileShare(map)
    }

    @Suppress("UNUSED_PARAMETER")
    @ToJson
    fun shareToJson(fileShare: FileShare) : String {
        throw UnsupportedOperationException("Serialisation of FileShare is not supported")
    }

    private fun processChannel(reader: JsonReader) : Pair<String, BigDecimal> {
        val id = reader.nextName()
        var lowestTs: BigDecimal? = null

        reader.iterateArray {
            reader.iterateObject {
                if (reader.nextName() != "ts") {
                    reader.skipValue()
                } else {
                    val ts = BigDecimal(reader.nextString())
                    lowestTs = lowestTs?.min(ts) ?: ts
                }
            }
        }

        return Pair(id, lowestTs!!)
    }
}