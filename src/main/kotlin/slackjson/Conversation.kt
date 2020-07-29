package slackjson

import com.squareup.moshi.*
import dagger.Lazy
import slack.Settings
import slack.SlackData
import slackjson.ConversationType.*
import javax.inject.Inject

enum class ConversationType(val shortName: String) {
    PUBLIC_CHANNEL("public"),
    PRIVATE_CHANNEL("private"),
    DIRECT_MESSAGE("dm");

    companion object {
        fun optionStr() = values().joinToString(", ", transform = { it.shortName.toUpperCase() })
    }
}

@MoshiInject
abstract class Conversation {
    abstract val id: String
    abstract val type: ConversationType

    abstract fun nameRaw(): String
    abstract fun namePrefixed(): String
}

class ConversationDm(
        override val id: String,
        private val userId: String
): Conversation() {
    override val type: ConversationType = DIRECT_MESSAGE

    // TODO can we break this dependency cycle and not need lazy?
    @Transient
    @Inject
    lateinit var slackData: Lazy<SlackData>

    @Transient
    @Inject
    lateinit var settings: Settings

    override fun nameRaw(): String {
        return if(settings.useDisplayNamesForConversationNames) {
            slackData.get().userDisplayname(userId)
        } else {
            slackData.get().userUsername(userId)
        }
    }

    override fun namePrefixed(): String {
        return "@${nameRaw()}"
    }
}

class ConversationChannel(
        override val id: String,
        override val type: ConversationType,
        private val name: String
) : Conversation() {
    init {
        if (type != PUBLIC_CHANNEL && type != PRIVATE_CHANNEL) {
            throw JsonDataException("Channels must have conversation type public or private")
        }
    }

    override fun nameRaw() = name
    override fun namePrefixed() = "#$name"
}


@JsonClass(generateAdapter = true)
class ParsedConversationExport(
        val id: String,
        val name: String?,

        // User field if dm
        @Json(name = "user")
        val userId: String?
) {
    fun toConversation(type: ConversationType): Conversation {
        return when (type) {
            PUBLIC_CHANNEL, PRIVATE_CHANNEL -> ConversationChannel(id, type, name!!)
            DIRECT_MESSAGE -> ConversationDm(id, userId!!)
        }
    }
}

@JsonClass(generateAdapter = true)
class ParsedConversationWeb(
        val id: String,
        val name: String?,

        // Conversation type
        @Json(name = "is_channel")
        val isChannel: Boolean = false,
        @Json(name = "is_group")
        val isGroup: Boolean = false,
        @Json(name = "is_im")
        val isIm: Boolean = false,

        // User field if dm
        @Json(name = "user")
        val userId: String?
) {
    init {
        val numTrues = booleanArrayOf(isChannel, isGroup, isIm).sumBy { if (it) 1 else 0 }
        val conversationStr = "Conversation $id" + if (name != null) " ($name)" else ""

        // Must be a channel, group, or dm
        if (numTrues == 0) {
            throw JsonDataException("$conversationStr is not a channel, private group, or dm")
        } else if (numTrues > 1) {
            throw JsonDataException("$conversationStr has more than 1 channel type")
        }

        // If it's a dm, then we must know the user
        if (isIm && userId == null) {
            throw JsonDataException("$conversationStr is a dm, but does not contain a user field")
        }
    }

    fun toConversation(): Conversation {
        return when (val type = conversationType()) {
            PUBLIC_CHANNEL, PRIVATE_CHANNEL -> ConversationChannel(id, type, name!!)
            DIRECT_MESSAGE -> ConversationDm(id, userId!!)
        }
    }

    private fun conversationType() = when {
        isChannel -> PUBLIC_CHANNEL
        isGroup -> PRIVATE_CHANNEL
        else -> DIRECT_MESSAGE
    }
}

object ConversationContextfulAdapter {
    @FromJson
    fun fromJson(conversationParsed: ParsedConversationWeb): Conversation {
        return conversationParsed.toConversation()
    }

    @ToJson
    @Suppress("UNUSED_PARAMETER")
    fun toJson(conversation: Conversation): String {
        throw UnsupportedOperationException("Serialisation of Conversation is not supported")
    }
}