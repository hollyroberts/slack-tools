package json.slack.message

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class MeMessage(
    val text: String
) : BaseUserMessage()