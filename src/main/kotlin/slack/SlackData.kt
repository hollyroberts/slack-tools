@file:Suppress("MemberVisibilityCanBePrivate")

package slack
import slackjson.*

abstract class SlackData(val settings: Settings) {
    // Raw data
    abstract val conversations: Map<String, Conversation>
    abstract val users: Map<String, User>

    // Basic data retrieval methods
    fun conversationName(convoId: String?) = conversations[convoId]?.fullName(this) ?: "Unknown conversation"
    fun conversationType(convoId: String) : ConversationTypes {
        val convo = conversations[convoId] ?: error("Unknown conversation ID '$convoId'")
        return convo.conversationType()
    }

    fun userUsername(userId: String?) = users[userId]?.username() ?: "Unknown user"
    fun userDisplayname(userId: String?) = users[userId]?.displayname() ?: "Unknown name"
}

data class Settings(
        val useDisplayNamesForConversationNames: Boolean = true,
        val useDisplayNamesForFiles: Boolean = false,

        val inferFileLocation: Boolean = true,
        val ignoreDownloadedFiles: Boolean = true
)
