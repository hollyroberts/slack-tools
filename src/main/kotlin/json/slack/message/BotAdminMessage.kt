package json.slack.message

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BotAdminMessage (
        val text: String
) : BaseUserMessage()