package json.slack.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BotMessage(
    val username: String?,
    @Json(name = "bot_id")
    val botId: String?,
    val text: String
) : BaseMessage()