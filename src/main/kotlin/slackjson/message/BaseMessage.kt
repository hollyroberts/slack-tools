package slackjson.message

import com.squareup.moshi.*
import org.apache.logging.log4j.kotlin.Logging

interface BaseMessage {
    val ts: String
}

interface BaseUserMessage : BaseMessage {
    val user: String
}

object BaseMessageCustomAdapter : Logging {
    private val keys = JsonReader.Options.of("type", "subtype")

    @FromJson
    fun fromJson(reader: JsonReader, textMessageAdapter: JsonAdapter<TextMessage>, channelMessageAdapter: JsonAdapter<ChannelMessage>): BaseMessage? {
        // Read type/subtype with peeked reader
        val peekedReader = reader.peekJson()

        var type: String? = null
        var subtype: String? = null

        // TODO Use extension functions to consume object when kotlin supports non-local breaks
        peekedReader.beginObject()
        while (peekedReader.hasNext()) {
            when (peekedReader.selectName(keys)) {
                // It might be worth figuring out if we can make this use Options to improve performance?
                // The issue is mapping the various types/subtypes to numbers and retaining the switch lookup performance
                // For the type we could check if it's 1/0
                // For the subtype we'd have to figure out the available subtypes defined, and then use that for our map

                0 -> type = peekedReader.nextString();
                1 -> subtype = peekedReader.nextString()
                -1 -> peekedReader.skipValue()
            }

            if (type != null && subtype != null) {
                break
            }
        }
        // Don't call endObject on the peeked reader

        // Parse message into actual type
        if (type != "message") {
            throw JsonDataException("Message type was not 'message', but was '$type'")
        }

        // TODO extend this
        return when (MessageType.lookup(subtype)) {
            Other.STANDARD_MESSAGE -> textMessageAdapter.fromJson(reader)
            is ChannelType -> channelMessageAdapter.fromJson(reader)
            else -> {
                // Since our list of subtypes is currently non-exhaustive then skip processing the message
                logger.trace { "Cannot process message subtype '${subtype}'" }
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(baseMessage: BaseMessage) : String {
        throw UnsupportedOperationException("Serialisation of BaseMessage is not supported")
    }
}