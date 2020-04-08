package slackjson

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import slack.Settings
import slack.SlackData
import javax.inject.Inject

enum class ConversationTypes(val shortName: String) {
    PUBLIC_CHANNEL("public"),
    PRIVATE_CHANNEL("private"),
    DIRECT_MESSAGE("dm"),
    UNKNOWN("unknown");

    companion object {
        fun optionStr() = values().joinToString(", ", transform = { it.shortName.toUpperCase() })
    }
}

@MoshiInject
@JsonClass(generateAdapter = true)
class Conversation(
        // Main data we're interested in
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
        val user: String?
) {
    @Transient
    @Inject
    lateinit var slackData: SlackData

    @Transient
    @Inject
    lateinit var settings: Settings

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
        if (isIm && user == null) {
            throw JsonDataException("$conversationStr is a dm, but does not contain a user field")
        }
    }

    /**
     * Returns enum indicating channel type
     */
    fun conversationType() = when {
        isChannel -> ConversationTypes.PUBLIC_CHANNEL
        isGroup -> ConversationTypes.PRIVATE_CHANNEL
        else -> ConversationTypes.DIRECT_MESSAGE
    }

    /**
     * Returns name depending on settings given
     */
    private fun name() = if (settings.useDisplayNamesForConversationNames) {
        slackData.userDisplayname(user)
    } else {
        slackData.userUsername(user)
    }

    /**
     * Returns conversation name prefixed with # or @ depending on whether it's a channel or dm
     */
    fun fullName() : String {
        return if (isChannel || isGroup) {
            "#$name"
        } else {
            "@${name()}"

        }
    }
}