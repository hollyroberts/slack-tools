package slack

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.inject.assisted.dagger2.AssistedModule
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import org.apache.logging.log4j.kotlin.Logging
import slackjson.Conversation
import slackjson.ConversationType.PUBLIC_CHANNEL
import slackjson.JsonLoader
import slackjson.ParsedConversationExport
import slackjson.User
import utils.Log
import utils.reifiedAdapter
import java.nio.file.Path
import javax.inject.Named
import javax.inject.Singleton

class SlackExport @AssistedInject constructor(
        @Assisted override val users: Map<String, User>,
        @Assisted override val conversations: Map<String, Conversation>
) : SlackData() {

    @AssistedInject.Factory
    interface Factory {
        fun create(users: Map<String, User>,
                   conversations: Map<String, Conversation>
        ): SlackExport
    }

    companion object : Logging

    @AssistedModule
    @Module(includes = [AssistedInject_SlackExport_Provider::class])
    object Provider {
        private const val USERS_FILE = "users.json"
        private const val CHANNELS_FILE = "channels.json"

        @Provides
        @Singleton
        fun loadFromFolder(@Named("FolderLocation") folder: Path, moshi: Moshi, factory: Factory): SlackExport {
            logger.log(Log.HIGH) { "Loading export metadata" }

            val userAdapter = moshi.reifiedAdapter<List<User>>()
            val userMap = JsonLoader.loadJson(folder.resolve(USERS_FILE), userAdapter)
                    .associateBy { it.id }
            logger.info { "Loaded data about ${userMap.size} users from $USERS_FILE" }

            val convoAdapter = moshi.reifiedAdapter<List<ParsedConversationExport>>()
            val convoMap = JsonLoader.loadJson(folder.resolve("channels.json"), convoAdapter)
                    .map { it.toConversation(PUBLIC_CHANNEL) }
                    .associateBy { it.id }
            logger.info { "Loaded data about ${convoMap.size} users from $CHANNELS_FILE" }

            return factory.create(userMap, convoMap)
        }
    }
}