package json.slack.message

import com.squareup.moshi.*

@JsonClass(generateAdapter = true)
class SlackBotMessage(
        val text: String
) : BaseMessage()

// TODO would it be possible to annotate/interface a json class as requiring post initialisation logic
// Wouldn't really save us much here but would be nice
// Or something else to avoid doing this
object SlackBotMessageAdapter {
    private val userKeys = JsonReader.Options.of("user")!!
    private const val SLACKBOT_USERNAME = "USLACKBOT"

    @FromJson
    fun fromJson(reader: JsonReader, slackbotMessageAdapter: JsonAdapter<SlackBotMessage>): SlackBotMessage {
        val peekedReader = reader.peekJson()

        peekedReader.beginObject()
        while (peekedReader.hasNext()) {
            when (peekedReader.selectName(userKeys)) {
                0 -> {
                    if (peekedReader.nextString() != SLACKBOT_USERNAME) {
                        throw JsonDataException("Slackbot messages must have user type of '$SLACKBOT_USERNAME'")
                    }
                    return slackbotMessageAdapter.fromJson(reader)!!
                }
                -1 -> peekedReader.skipValue()
            }
        }

        throw JsonDataException("Did not encounter user field for slackbot response message type")
    }
}