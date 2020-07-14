package network

import network.SlackTier.*
import org.apache.logging.log4j.kotlin.Logging
import retrofit2.http.GET
import retrofit2.http.Query
import slackjson.*
import slackjson.message.BaseMessage
import utils.Log
import utils.formatSize
import java.time.Instant
import java.time.ZonedDateTime

interface SlackApi {
    companion object : Logging {
        private val ALL_CONVERSATION_TYPES  = listOf("public_channel", "private_channel", "im").joinToString()
    }

    @GET("conversations.history?limit=100&inclusive=true")
    @Slack(TIER_3)
    fun getConversationHistoryPage(
            @Query("cursor") cursor: String?,
            @Query("channel") conversation: String,
            @Query("oldest") start: Long,
            @Query("latest") end: Long
    ) : ConversationHistoryResponse

    @JvmDefault
    fun getConversationHistory(
            conversation: String,
            start: Instant,
            end: Instant
    ) {
        logger.log(Log.LOW) { "Retrieving conversation history for $conversation" }
        ZonedDateTime.now().toEpochSecond()
        val messages: List<BaseMessage> = Pagination.retrieveCursorResponseAsList(
                "messages for $conversation",
                pageRetrievalFun = { cursor ->
                    getConversationHistoryPage(
                            cursor = cursor,
                            conversation = conversation,
                            start = start.epochSecond,
                            end = end.epochSecond
                    )
                },
                appenderFun = {
                    it.asSequence().filterNotNull()
                }
        )
        logger.log(Log.LOW) { "Retrieved ${messages.size} messages for $conversation"}
    }

    @GET("conversations.list?limit=100")
    @Slack(TIER_2)
    fun listConversationsPage(
            @Query("cursor") cursor: String?,
            @Query("types") types: String
    ): ConversationListResponse

    @JvmDefault
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

    @JvmDefault
    fun listFiles(
            startTime: Long? = null,
            endTime: Long? = null,
            channel: String? = null,
            user: String? = null
    ): List<ParsedFile> {
        logger.info { "Retrieving files" }
        val list = Pagination.retrievePaginatedList(
                "files",
                pageRetrievalFun = {
                    listFilesPage(
                            page = it,
                            startTime = startTime,
                            endTime = endTime,
                            channel = channel,
                            user = user
                    )
                }
        )
        val fileSize = list.map { it.size }.sum()
        logger.info { "Retrieved info for %,d files (%s)".format(list.size, formatSize(fileSize)) }
        return list
    }

    @GET("users.list")
    @Slack(TIER_2)
    fun listUsersPage(@Query("cursor") cursor: String?): UserListResponse

    @JvmDefault
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
}