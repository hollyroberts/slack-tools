package slackjson.message

import com.squareup.moshi.*
import slackjson.message.MessageSubtype.STANDARD_MESSAGE

interface BaseMessage {
    val ts: String
}

interface BaseUserMessage : BaseMessage {
    val user: String
}

object BaseMessageCustomAdapter {
    private val moshi = Moshi.Builder().build()
    private val textMessageAdapter = moshi.adapter(TextMessage::class.java)

    private val keys = JsonReader.Options.of("type", "subtype")

    @FromJson
    fun fromJson(reader: JsonReader): BaseMessage? {
        // Read type/subtype with peeked reader
        val peekedReader = reader.peekJson()

        var type: String? = null
        var subtype: String? = null

        peekedReader.beginObject()
        while (peekedReader.hasNext()) {
            when (peekedReader.selectName(keys)) {
                0 -> type = peekedReader.nextString()
                1 -> subtype = peekedReader.nextString()
                -1 -> {
                    peekedReader.skipValue()
                }
            }

            if (type != null && subtype != null) {
                break
            }
        }
        peekedReader.endObject()

        // Parse message into actual type
        if (type != "message") {
            throw JsonDataException("Message type was not 'message', but was '$type'")
        }
        val subtypeEnum = MessageSubtype.enmMap.getOrElse(subtype, { return null })

        // TODO extend this
        return when (subtypeEnum) {
            STANDARD_MESSAGE -> textMessageAdapter.fromJson(reader)
            else -> null
        }
    }

    @ToJson
    fun toJson(baseMessage: BaseMessage) : String {
        throw JsonEncodingException("Encoding to Json not supported")
    }
}

enum class MessageSubtype(val label: String?) {
    STANDARD_MESSAGE(null),
    CHANNEL_JOIN("channel_join"),
    CHANNEL_LEAVE("channel_leave"),
    CHANNEL_NAME("channel_name"),
    CHANNEL_PURPOSE("channel_purpose"),
    CHANNEL_TOPIC("channel_topic");

    companion object {
        val enmMap = values().associateBy { it.label }
    }
}

fun main() {
    val text: String = """{
    "type": "message",
    "channel": "C2147483705",
    "user": "U2147483697",
    "text": "Hello world",
    "ts": "1355517523.000005"
}"""

    val adapter = Moshi.Builder()
            .add(BaseMessageCustomAdapter)
            .build()
            .adapter(BaseMessage::class.java)

    val parsed = adapter.fromJson(text)!!
}