package network

import json.*
import json.slack.file.ParsedFile
import json.slack.message.BaseMessage
import json.slack.metadata.Conversation
import json.slack.metadata.User
import network.SlackTier.*
import network.body.FileId
import org.apache.logging.log4j.kotlin.Logging
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import utils.Log
import utils.formatSize
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

interface SlackApi {
  companion object : Logging {
    private val ALL_CONVERSATION_TYPES = listOf("public_channel", "private_channel", "im").joinToString()

    private val LOCAL_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
  }

  @GET("conversations.history?limit=100&inclusive=true")
  @Slack(TIER_3)
  fun getConversationHistoryPage(
      @Query("cursor") cursor: String?,
      @Query("channel") conversation: String,
      @Query("oldest") start: Long?,
      @Query("latest") end: Long?
  ): ConversationHistoryResponse

  fun getConversationHistory(
      conversation: Conversation,
      start: Instant? = null,
      end: Instant? = null
  ): List<BaseMessage> {
    if (start != null && end != null) {
      require(start.isBefore(end)) { "Start must be before end" }
    }

    logger.log(Log.LOW) { "Retrieving conversation history for ${conversation.namePrefixed()}" }

    var totalMessagesSeen = 0
    val messages: MutableList<BaseMessage> = Pagination.retrieveCursorResponseAsList(
        "messages for ${conversation.namePrefixed()}",
        pageRetrievalFun = { cursor ->
          getConversationHistoryPage(
              cursor = cursor,
              conversation = conversation.id,
              start = start?.epochSecond,
              end = end?.epochSecond
          )
        },
        appenderFun = {
          totalMessagesSeen += it.size
          it.asSequence().filterNotNull()
        }
    )

    logger.log(Log.LOW) { "Retrieved ${messages.size} messages for ${conversation.namePrefixed()}" }

    val skippedMessages = totalMessagesSeen - messages.size
    if (skippedMessages > 0) {
      logger.warn { "Skipped $skippedMessages/$totalMessagesSeen messages for ${conversation.namePrefixed()}" }
    }

    messages.reverse()
    return messages
  }

  @GET("conversations.list?limit=100")
  @Slack(TIER_2)
  fun listConversationsPage(
      @Query("cursor") cursor: String?,
      @Query("types") types: String
  ): ConversationListResponse

  fun listConversations(): Map<String, Conversation> {
    logger.info { "Retrieving conversations (channels)" }
    val convoMap = Pagination.retrieveCursorResponseAsMap(
        "conversations",
        pageRetrievalFun = {
          listConversationsPage(cursor = it, types = ALL_CONVERSATION_TYPES)
        },
        mappingFun = { it.id }
    )
    logger.info { "Finished retrieving conversations (${convoMap.size} found)" }
    return convoMap
  }

  @GET("files.info")
  @Slack(TIER_4)
  @UseWrapper(FileResponse::class)
  fun listFileInfo(@Query("file") fileId: String): ParsedFile

  @GET("files.list?count=100")
  @Slack(TIER_3)
  fun listFilesPage(
      @Query("page") page: Int = 1,
      @Query("ts_from") startTime: Long? = null,
      @Query("ts_to") endTime: Long? = null,
      @Query("channel") channel: String? = null,
      @Query("user") user: String? = null
  ): FileListResponse

  fun listFiles(
      startTime: ZonedDateTime? = null,
      endTime: ZonedDateTime? = null,
      channel: String? = null,
      user: String? = null
  ): List<ParsedFile> {
    var timePeriod: String = ""
    startTime?.let { timePeriod += " after ${it.format(LOCAL_DATE_TIME)}" }
    if (startTime != null && endTime != null) {
      timePeriod += " and"
    }
    endTime?.let { timePeriod += " before ${it.format(LOCAL_DATE_TIME)}" }

    logger.info { "Retrieving files$timePeriod" }
    val list = Pagination.retrievePaginatedList(
        "files",
        pageRetrievalFun = {
          listFilesPage(
              page = it,
              startTime = startTime?.toEpochSecond(),
              endTime = endTime?.toEpochSecond(),
              channel = channel,
              user = user
          )
        }
    )
    val fileSize = list.sumOf { it.size }
    logger.info { "Retrieved info for %,d files (%s)".format(list.size, formatSize(fileSize)) }
    return list
  }

  @GET("users.list")
  @Slack(TIER_2)
  fun listUsersPage(@Query("cursor") cursor: String?): UserListResponse

  fun listUsers(): Map<String, User> {
    logger.info { "Retrieving user results" }
    val results = Pagination.retrieveCursorResponseAsMap(
        name = "users",
        pageRetrievalFun = { listUsersPage(it) },
        mappingFun = { it.id }
    )
    logger.info { "Finished retrieving user results (${results.size} found)" }
    return results
  }

  @POST("files.delete")
  @Slack(TIER_3)
  fun deleteFile(@Body file: FileId): SlackStatusResponse
}