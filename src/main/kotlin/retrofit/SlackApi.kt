package retrofit

import org.apache.logging.log4j.kotlin.Logging
import retrofit.SlackTier.*
import retrofit2.http.GET
import retrofit2.http.Query
import slackjson.*
import utils.formatSize

interface SlackApi {
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
        logger.info { "Retrieved %,d files (%s)".format(list.size, formatSize(fileSize)) }
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

    companion object : Logging
}