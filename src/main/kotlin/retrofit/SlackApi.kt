package retrofit

import org.apache.logging.log4j.kotlin.Logging
import retrofit.SlackTier.TIER_3
import retrofit.SlackTier.TIER_4
import retrofit2.http.GET
import retrofit2.http.Query
import slackjson.FileListResponse
import slackjson.FileResponse
import slackjson.ParsedFile
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
    ) = Pagination.retrievePaginatedList(
            "files",
            pageRetrievalFun = {
                listFilesPage(
                        page = it,
                        startTime = startTime,
                        endTime = endTime,
                        channel = channel,
                        user = user
                )
            },
            postRetrievalFun = { list ->
                val fileSize = list.map { it.size }.sum()
                logger.info { "Retrieved %,d files (%s)".format(list.size, formatSize(fileSize)) }
            }
    )

    companion object : Logging
}