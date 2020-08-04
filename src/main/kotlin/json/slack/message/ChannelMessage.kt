package json.slack.message

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
class ChannelMessage(
        override val ts: BigDecimal,
        override val user: String,

        val text: String
) : BaseUserMessage {
    @Transient
    override lateinit var subtype: MessageType
}