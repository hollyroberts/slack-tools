package json.slack.message

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BotMessage(
        val username: String,
        val text: String
) : BaseMessage()