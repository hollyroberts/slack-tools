package json.slack.message

import com.squareup.moshi.*
import org.apache.logging.log4j.kotlin.Logging
import javax.inject.Inject
import javax.inject.Singleton

// TODO put this back to an object depending on what we choose to do
@Singleton
class SlackMessageAdapter @Inject constructor(
        private val typeRecorder: MessageTypeRecorder
) {
    companion object : Logging {
        private val keys = JsonReader.Options.of("type", "subtype")
    }

    // TODO maybe better to lazily initialise the adapters?
    // Do some performance analysis on this when we have more adapters?
    @FromJson
    fun fromJson(
            reader: JsonReader,
            textMessageAdapter: JsonAdapter<TextMessage>,
            channelMessageAdapter: JsonAdapter<ChannelMessage>,
            botMessageAdapter: JsonAdapter<BotMessage>,
            botAdminMessageAdapter: JsonAdapter<BotAdminMessage>,
            meMessageAdapter: JsonAdapter<MeMessage>,
            reminderAddAdapter: JsonAdapter<ReminderAddMessage>,
            slackbotMessageAdapter: JsonAdapter<SlackBotMessage>
    ): BaseMessage? {
        // Read type/subtype with peeked reader
        val peekedReader = reader.peekJson()

        var type: String? = null
        var subtypeStr: String? = null

        // We don't use JsonReader.selectName() for this because it's less efficient
        // The internal Okio select implementation operates on bytes rather than UTF-8
        // This means that if it doesn't match (ie. -1) it needs to parse the name anyway and then do a more standard comparison
        // Even worse the comparison is done by looping over each options value and checking for equality (rather than say a map lookup)
        // In the majority of cases messages don't have subtypes, so we need to peek the entire object and all its keys
        // The above means that this is actually a lot less efficient that just reading the name and doing a switch lookup
        peekedReader.beginObject()
        while (peekedReader.hasNext()) {
            when (peekedReader.nextName()) {
                // It might be worth figuring out if we can use Options for type/subtype to improve performance?
                // The issue is mapping the various types/subtypes to numbers and retaining the switch lookup performance
                // For the type we could check if it's 1/0
                // For the subtype we'd have to figure out the available subtypes defined, and then use that for our map
                "type" -> {
                    type = peekedReader.nextString()
                    if (type != null && subtypeStr != null) break
                }
                "subtype" -> {
                    subtypeStr = peekedReader.nextString()
                    if (type != null && subtypeStr != null) break
                }
                else -> peekedReader.skipValue()
            }
        }
        // We don't need to call endObject as the peeked reader will be discarded

        // Parse message into actual type
        if (type != "message") {
            throw JsonDataException("Message type was not 'message', but was '$type'")
        }
        typeRecorder.recordType(subtypeStr)

        // TODO extend this
        // TODO could this be quicker with some sort of mapping lookup? Either a direct switch, or something else
        // For example either a mapping of types, or coalescing the enums into one and having sets of types
        val subtype = MessageType.lookup(subtypeStr)
        val message: BaseMessage = when (subtype) {
            OtherEvent.STANDARD_MESSAGE -> textMessageAdapter.fromJson(reader)
            OtherEvent.BOT_MESSAGE -> botMessageAdapter.fromJson(reader)
            OtherEvent.ME_MESSAGE -> meMessageAdapter.fromJson(reader)
            OtherEvent.REMINDER_ADD -> reminderAddAdapter.fromJson(reader)
            OtherEvent.SLACKBOT_RESPONSE -> slackbotMessageAdapter.fromJson(reader)
            is ChannelEvent -> channelMessageAdapter.fromJson(reader)
            is BotAdminEvent -> botAdminMessageAdapter.fromJson(reader)
            else -> {
                // Since our list of subtypes is currently non-exhaustive then skip processing the message
                logger.debug { "Cannot process message subtype '${subtype}'" }
                reader.skipValue()
                return null
            }
        }!!
        message.subtype = subtype
        return message
    }

    @ToJson
    @Suppress("UNUSED_PARAMETER")
    fun toJson(baseMessage: BaseMessage) : String {
        throw UnsupportedOperationException("Serialisation of BaseMessage is not supported")
    }
}