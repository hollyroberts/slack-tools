package slackjson.message

interface MessageType {
    val label: String?

    companion object {
        private val typeMap: Map<String?, MessageType> = listOf<List<MessageType>>(
                ChannelType.values().toList(),
                Other.values().toList()
        )
                .flatten()
                .associateBy { it.label }

        fun lookupStrict(subtype: String?): MessageType {
            return typeMap[subtype] ?: throw IllegalArgumentException("Unrecognised subtype '$subtype'")
        }

        fun lookup(subtype: String?): MessageType? {
            return typeMap[subtype]
        }
    }
}

enum class ChannelType(override val label: String) : MessageType {
    CHANNEL_JOIN("channel_join"),
    CHANNEL_LEAVE("channel_leave"),
    CHANNEL_NAME("channel_name"),
    CHANNEL_PURPOSE("channel_purpose"),
    CHANNEL_TOPIC("channel_topic");
}

enum class Other(override val label: String?) : MessageType {
    STANDARD_MESSAGE(null)
}