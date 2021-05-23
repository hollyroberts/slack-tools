package slack

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import json.JsonLoader
import json.reifiedAdapter
import json.slack.metadata.Conversation
import json.slack.metadata.ConversationType.PUBLIC_CHANNEL
import json.slack.metadata.ParsedConversationExport
import json.slack.metadata.User
import org.apache.logging.log4j.kotlin.Logging
import utils.Log
import java.nio.file.Path
import javax.inject.Named
import javax.inject.Singleton

class SlackExport constructor(
    override val users: Map<String, User>,
    override val conversations: Map<String, Conversation>
) : SlackData() {

  companion object : Logging

  @Module
  object Provider {
    private const val USERS_FILE = "users.json"
    private const val CHANNELS_FILE = "channels.json"

    @Provides
    @Singleton
    fun loadFromFolder(@Named("FolderLocation") folder: Path, moshi: Moshi): SlackExport {
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

      return SlackExport(userMap, convoMap)
    }
  }
}