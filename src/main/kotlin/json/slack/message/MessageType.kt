package json.slack.message

interface MessageType {
    val label: String?

    companion object {
        private val typeMap: Map<String?, MessageType> = listOf<List<MessageType>>(
                ChannelEvent.values().toList(),
                OtherEvent.values().toList()
        )
                .flatten()
                .associateBy { it.label }

        // Maybe keep this around a bit, but we probably shouldn't need it now we have the subtype for everything
        fun lookupStrict(subtype: String?): MessageType {
            return typeMap[subtype] ?: throw IllegalArgumentException("Unrecognised subtype '$subtype'")
        }

        fun lookup(subtype: String?): MessageType? {
            return typeMap[subtype]
        }
    }
}

enum class ChannelEvent(override val label: String) : MessageType {
    CHANNEL_ARCHIVE("channel_archive"),
    CHANNEL_JOIN("channel_join"),
    CHANNEL_LEAVE("channel_leave"),
    CHANNEL_NAME("channel_name"),
    CHANNEL_PURPOSE("channel_purpose"),
    CHANNEL_TOPIC("channel_topic"),
    CHANNEL_UNARCHIVE("channel_unarchive");
}

enum class BotAdminEvent(override val label: String) : MessageType {
    BOT_ADD("bot_add"),
    BOT_DISABLE("bot_disable"),
    BOT_ENABLE("bot_enable"),
    BOT_REMOVE("bot_remove")
}

enum class ThreadEvent(override val label: String?) : MessageType {
    REPLY_BROADCAST("reply_broadcast"),
    THREAD_BROADCAST("thread_broadcast")
}

enum class OtherEvent(override val label: String?) : MessageType {
    STANDARD_MESSAGE(null),
    BOT_MESSAGE("bot_message"),
    ME_MESSAGE("me_message"), // TODO this can probably be a normal message, but that italicizes itself
    PINNED_ITEM("pinned_item"),
    SLACKBOT_RESPONSE("slackbot_response"),
    REMINDER_ADD("reminder_add"),
    TOMBSTONE("tombstone")
}