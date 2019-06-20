@file:Suppress("MemberVisibilityCanBePrivate")

package slack
import slackjson.*

abstract class SlackData(val settings: Settings) {
    // Raw data
    abstract val conversations: Map<String, Conversation>
    abstract val users: Map<String, User>

    // Basic data retrieval methods
    fun getConversationName(convoId: String?) = conversations[convoId]?.getFullName(this) ?: "Unknown conversation"
    fun getConversationType(convoId: String) : ConversationTypes {
        val convo = conversations[convoId] ?: error("Unknown conversation ID '$convoId'")
        return convo.getConversationType()
    }

    fun getUserUsername(userId: String?) = users[userId]?.name ?: "Unknown user"
    fun getUserProfilename(userId: String?) = users[userId]?.profile?.displayName ?: "Unknown name"
}

data class Settings(
        val useProfileNamesForConversationNames: Boolean = true,
        val useProfileNamesForFiles: Boolean = false,

        val inferFileLocation: Boolean = true,
        val ignoreDownloadedFiles: Boolean = true
)
