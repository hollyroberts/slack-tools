@file:Suppress("MemberVisibilityCanBePrivate")

package slack
import slackjson.*

abstract class SlackData(val settings: Settings) {
    // Raw data
    abstract val conversations: Map<String, Conversation>
    abstract val filesParsed: List<ParsedFile>
    abstract val users: Map<String, User>

    // Responses may need further processing
    /**
     * Map of file id to complete files
     */
    abstract val filesComplete: Map<String, CompleteFile>

    /**
     * Map of conversation id --> list of files
     */
    val filesByConvo by lazy {
        val filesConvo = mutableMapOf<String?, MutableList<CompleteFile>>()

        filesComplete.values.forEach {
            val uploadLoc = it.uploadLoc // Key will be null if we don't know the convo
            filesConvo.getOrPut(uploadLoc) { mutableListOf() }
                    .add(it)
        }

        return@lazy filesConvo.toMap()
    }

    // Basic data retrieval methods
    fun getUsername(userId: String?) = users[userId]?.name ?: "Unknown user"
    fun getConversationName(convoId: String?) = conversations[convoId]?.getFullName(this) ?: "Unknown conversation"
}

