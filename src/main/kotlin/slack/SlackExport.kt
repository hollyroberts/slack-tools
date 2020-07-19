package slack

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.buffer
import okio.source
import org.apache.logging.log4j.kotlin.Logging
import slackjson.Conversation
import slackjson.User
import java.nio.file.Path

class SlackExport private constructor(
        override val users: Map<String, User>,
        override val conversations: Map<String, Conversation>
) : SlackData() {
    // Factory method
    companion object : Logging {
        fun loadFromFolder(folder: Path, moshi: Moshi): SlackExport {
            val userAdapter: JsonAdapter<List<User>> = moshi.adapter(Types.newParameterizedType(List::class.java, User::class.java))
            val userMap = loadJson(folder.resolve("users.json"), userAdapter)
                    .associateBy { it.id }

            println(userMap.size)
            val convoAdapter: JsonAdapter<List<Conversation>> = moshi.adapter(Types.newParameterizedType(List::class.java, Conversation::class.java))
            val convoMap = loadJson(folder.resolve("channels.json"), convoAdapter)
                    .associateBy { it.id }
            println(convoMap.size)

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