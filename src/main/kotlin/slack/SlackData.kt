@file:Suppress("MemberVisibilityCanBePrivate")

package slack
import slackjson.*

abstract class SlackData(val settings: Settings) {
    // Raw data
    abstract val conversations: Map<String, Conversation>
    abstract val users: Map<String, User>

    // Basic data retrieval methods
    fun getUsername(userId: String?) = users[userId]?.name ?: "Unknown user"
    fun getConversationName(convoId: String?) = conversations[convoId]?.getFullName(this) ?: "Unknown conversation"
}

data class Settings(
        val inferFileLocation: Boolean = true,
        val ignoreDownloadedFiles: Boolean = true
)
