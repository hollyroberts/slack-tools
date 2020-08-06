package json.slack.message

interface MessageType {
    val label: String?

    companion object {
        private val typeMap: Map<String?, MessageType> = listOf<List<MessageType>>(
                ChannelType.values().toList(),
                Other.values().toList()
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

enum class ChannelType(override val label: String) : MessageType {
    CHANNEL_ARCHIVE("channel_archive"),
    CHANNEL_JOIN("channel_join"),
    CHANNEL_LEAVE("channel_leave"),
    CHANNEL_NAME("channel_name"),
    CHANNEL_PURPOSE("channel_purpose"),
    CHANNEL_TOPIC("channel_topic"),
    CHANNEL_UNARCHIVE("channel_unarchive");
}

enum class Other(override val label: String?) : MessageType {
    STANDARD_MESSAGE(null)
}