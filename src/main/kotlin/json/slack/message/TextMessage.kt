package json.slack.message

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class TextMessage(
        val text: String
) : BaseUserMessage()