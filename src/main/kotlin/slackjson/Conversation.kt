package slackjson

data class Conversation(
        // Main data we're interested in
        val id: String,
        val name: String,

        // Conversation type
        val is_channel: Boolean,
        val is_group: Boolean,
        val is_im: Boolean
)