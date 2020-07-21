package slack

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.buffer
import okio.source
import org.apache.logging.log4j.kotlin.Logging
import slackjson.Conversation
import slackjson.ConversationType.PUBLIC_CHANNEL
import slackjson.ParsedConversationExport
import slackjson.User
import java.nio.file.Path

class SlackExport private constructor(
        override val users: Map<String, User>,
        override val conversations: Map<String, Conversation>
) : SlackData() {
    // Factory method
    companion object : Logging {
        private const val USERS_FILE = "users.json"
        private const val CHANNELS_FILE = "channels.json"

        fun loadFromFolder(folder: Path, moshi: Moshi): SlackExport {
            val userAdapter: JsonAdapter<List<User>> = moshi.adapter(Types.newParameterizedType(List::class.java, User::class.java))
            val userMap = loadJson(folder.resolve(USERS_FILE), userAdapter)
                    .associateBy { it.id }
            logger.info { "Loaded data about ${userMap.size} users from $USERS_FILE" }

            val convoAdapter: JsonAdapter<List<ParsedConversationExport>> = moshi.adapter(Types.newParameterizedType(List::class.java, ParsedConversationExport::class.java))
            val convoMap = loadJson(folder.resolve("channels.json"), convoAdapter)
                    .map { it.toConversation(PUBLIC_CHANNEL) }
                    .associateBy { it.id }
            logger.info { "Loaded data about ${convoMap.size} users from $CHANNELS_FILE" }

            return SlackExport(userMap, convoMap)
        }

        private fun <T> loadJson(location: Path, adapter: JsonAdapter<T>): T {
            logger.trace { "Loading $location" }
            val file = location.toFile()
            val bufferedSource = file.source().buffer()
            return adapter.fromJson(bufferedSource)!!
        }
    }
}