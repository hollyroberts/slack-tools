package slack

import com.squareup.moshi.Moshi
import json.JsonLoader
import json.NullDroppingList
import json.reifiedAdapter
import json.slack.message.BaseMessage
import json.slack.metadata.Conversation
import org.apache.logging.log4j.kotlin.Logging
import utils.Log
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named

class SlackExportProcessor @Inject constructor(
    moshi: Moshi,
    @Named("FolderLocation") private val folder: Path
) {
  companion object : Logging {
    private const val MESSAGE_FILE_GLOB = "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].json"
  }

  class LoadStats(val messagesLoaded: Int, val messagesDropped: Int)

  private val messageAdapter by lazy { moshi.reifiedAdapter<NullDroppingList<BaseMessage>>() }

  fun loadConversationFolder(conversation: Conversation): LoadStats {
    logger.log(Log.LOW) { "Loading conversation history from ${conversation.namePrefixed()}" }

    val convoPath = folder.resolve(conversation.nameRaw())
    val fileList = Files.newDirectoryStream(convoPath, MESSAGE_FILE_GLOB).use { dirStream ->
      dirStream
          .filter(Files::isRegularFile)
          .sorted()
          .toList()
    }

    val messages = mutableListOf<BaseMessage>()
    var droppedMessages = 0
    fileList.forEach { file ->
      val fileMessages = JsonLoader.loadJson(convoPath.resolve(file.fileName), messageAdapter)
      droppedMessages += fileMessages.droppedItems
      messages.addAll(fileMessages)
    }

    // TODO do some testing of this performance
    // Hopefully since it should be in order then it'll be really quick
    var ts: BigDecimal? = null
    for (message in messages) {
      if (ts != null && message.ts <= ts) {
        error("Timestamp ${message.ts} is less than $ts")
      }

      ts = message.ts
    }

    if (droppedMessages > 0) {
      logger.warn {
        String.format("Dropped %,d message%s from %s",
            droppedMessages,
            if (droppedMessages > 1) "s" else "",
            conversation.namePrefixed()
        )
      }
    }
    logger.log(Log.LOW) { String.format("Loaded %,d messages from %s", messages.size, conversation.namePrefixed()) }

    return LoadStats(messages.size, droppedMessages)
  }
}