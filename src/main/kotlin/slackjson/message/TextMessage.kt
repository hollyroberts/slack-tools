package slackjson.message

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class TextMessage(
        override val ts: String,
        override val user: String,

        val text: String
) : BaseUserMessage