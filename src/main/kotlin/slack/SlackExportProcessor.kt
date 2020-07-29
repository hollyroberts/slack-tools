package slack

import com.squareup.moshi.Moshi
import org.apache.logging.log4j.kotlin.Logging
import slackjson.Conversation
import slackjson.JsonLoader
import slackjson.message.BaseMessage
import utils.Log
import utils.reifiedAdapter
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

    private val messageAdapter by lazy { moshi.reifiedAdapter<List<BaseMessage?>>() }

    fun loadConversationFolder(conversation: Conversation) {
        logger.log(Log.LOW) { "Loading conversation history from ${conversation.namePrefixed()}" }

        val convoPath = folder.resolve(conversation.nameRaw())
        val fileList = Files.newDirectoryStream(convoPath, MESSAGE_FILE_GLOB).use { dirStream ->
            dirStream
                    .filter { Files.isRegularFile(it) }
                    .sorted()
                    .toList()
        }

        val messages = mutableListOf<BaseMessage>()
        fileList.forEach {file ->
            val fileMessages = JsonLoader.loadJson(convoPath.resolve(file.fileName), messageAdapter)
            messages.addAll(fileMessages.filterNotNull())
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

        logger.log(Log.LOW) { String.format("Loaded %,d messages from %s", messages.size, conversation.namePrefixed()) }
    }
}