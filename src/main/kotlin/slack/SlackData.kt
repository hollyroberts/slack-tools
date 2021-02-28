@file:Suppress("MemberVisibilityCanBePrivate")

package slack

import json.slack.metadata.Conversation
import json.slack.metadata.ConversationType
import json.slack.metadata.User
import network.http.HttpUtils.ConflictStrategy
import scripts.TimeOptions
import java.time.ZoneId

abstract class SlackData {
  // Raw data
  abstract val conversations: Map<String, Conversation>
  abstract val users: Map<String, User>

  // Basic data retrieval methods
  fun conversationName(convoId: String?) = conversations[convoId]?.namePrefixed() ?: "Unknown conversation"
  fun conversationType(convoId: String?): ConversationType? {
    if (convoId == null) {
      return null
    }

    val convo = conversations[convoId] ?: error("Unknown conversation ID '$convoId'")
    return convo.type
  }

  fun userUsername(userId: String?) = users[userId]?.username() ?: "Unknown user"
  fun userDisplayname(userId: String?) = users[userId]?.displayname() ?: "Unknown name"

  // Inference methods
  fun inferUserID(userString: String): String? {
    // User ID first
    if (users.containsKey(userString)) {
      return userString
    }

    // Username if it exists
    users.asSequence().forEach {
      if (it.value.username() == userString) {
        return it.key
      }
    }

    // Displayname if it exists
    users.asSequence().forEach {
      if (it.value.displayname() == userString) {
        return it.key
      }
    }

    return null
  }

  fun inferChannelID(channelString: String): String? {
    if (conversations.containsKey(channelString)) {
      return channelString
    }

    conversations.forEach {
      if (it.value.namePrefixed() == channelString) {
        return it.key
      }
    }

    return null
  }
}

data class Settings(
    var useDisplayNamesForConversationNames: Boolean = true,
    var useDisplayNamesForFiles: Boolean = false,

    var fileConflictStrategy: ConflictStrategy = ConflictStrategy.default(),
    var ignoreDownloadedFiles: Boolean = true,

    var outTz: ZoneId = ZoneId.systemDefault()
) {
  fun applyTimeOptions(timeOptions: TimeOptions): Settings {
    outTz = timeOptions.outputTz
    return this
  }
}
