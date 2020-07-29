package slackjson.message

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
class TextMessage(
        override val ts: BigDecimal,
        override val user: String,

        val text: String
) : BaseUserMessage