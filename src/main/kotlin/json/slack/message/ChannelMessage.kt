package json.slack.message

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ChannelMessage(
    val text: String
) : BaseUserMessage()