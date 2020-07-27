package slack

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.inject.assisted.dagger2.AssistedModule
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okio.buffer
import okio.source
import org.apache.logging.log4j.kotlin.Logging
import slackjson.Conversation
import slackjson.ConversationType.PUBLIC_CHANNEL
import slackjson.ParsedConversationExport
import slackjson.User
import slackjson.message.BaseMessage
import utils.reifiedAdapter
import java.nio.file.Path
import javax.inject.Named
import javax.inject.Singleton

class SlackExport @AssistedInject constructor(
        moshi: Moshi,
        @Named("FolderLocation") folder: Path,
        @Assisted override val users: Map<String, User>,
        @Assisted override val conversations: Map<String, Conversation>
) : SlackData() {
    private val messageAdapter by lazy { moshi.reifiedAdapter<BaseMessage>() }

    @AssistedInject.Factory
    interface Factory {
        fun create(users: Map<String, User>,
                   conversations: Map<String, Conversation>
        ): SlackExport
    }

    companion object : Logging {
        private fun <T> loadJson(location: Path, adapter: JsonAdapter<T>): T {
            logger.trace { "Loading $location" }
            val file = location.toFile()
            val bufferedSource = file.source().buffer()
            return adapter.fromJson(bufferedSource)!!
        }
    }

    @AssistedModule
    @Module(includes = [AssistedInject_SlackExport_Provider::class])
    object Provider {
        private const val USERS_FILE = "users.json"
        private const val CHANNELS_FILE = "channels.json"

        @Provides
        @Singleton
        fun loadFromFolder(@Named("FolderLocation") folder: Path, moshi: Moshi, factory: Factory): SlackExport {
            val userAdapter = moshi.reifiedAdapter<List<User>>()
            val userMap = loadJson(folder.resolve(USERS_FILE), userAdapter)
                    .associateBy { it.id }
            logger.info { "Loaded data about ${userMap.size} users from $USERS_FILE" }

            val convoAdapter = moshi.reifiedAdapter<List<ParsedConversationExport>>()
            val convoMap = loadJson(folder.resolve("channels.json"), convoAdapter)
                    .map { it.toConversation(PUBLIC_CHANNEL) }
                    .associateBy { it.id }
            logger.info { "Loaded data about ${convoMap.size} users from $CHANNELS_FILE" }

            return factory.create(userMap, convoMap)
        }
    }
}