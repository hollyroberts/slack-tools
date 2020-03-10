package slackjson.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ChannelMessage(
        override val ts: String,
        override val user: String,

        @Deprecated(message = "Use subtype instead")
        @Json(name = "subtype")
        val subtypeRaw: String,

        val text: String
) : BaseUserMessage {
    @Suppress("DEPRECATION")
    val subtype = MessageType.lookup(subtypeRaw) as ChannelType
}