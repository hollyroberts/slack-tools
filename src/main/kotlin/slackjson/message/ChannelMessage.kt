package slackjson.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
class ChannelMessage(
        override val ts: BigDecimal,
        override val user: String,

        @Deprecated(message = "Use subtype instead")
        @Json(name = "subtype")
        val subtypeRaw: String,

        val text: String
) : BaseUserMessage {
    @Suppress("DEPRECATION")
    val subtype: ChannelType = MessageType.lookupStrict(subtypeRaw) as ChannelType
}