package json.slack.message

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ReminderAddMessage(
        val text: String
) : BaseUserMessage()