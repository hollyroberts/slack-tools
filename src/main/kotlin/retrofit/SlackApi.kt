package retrofit

import okhttp3.internal.toImmutableList
import org.apache.logging.log4j.kotlin.Logging
import retrofit.SlackTier.TIER_3
import retrofit.SlackTier.TIER_4
import retrofit2.http.GET
import retrofit2.http.Query
import slackjson.*
import utils.Log
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
    ) = retrievePaginatedList(
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

    companion object : Logging {
        internal fun <T> retrievePaginatedList(
                name: String,
                pageRetrievalFun: (Int) -> PaginatedResponse<T>,
                postRetrievalFun: (List<T>) -> Unit = {
                    logger.log(Log.HIGH) { "Retrieved %,d %s".format(it.size, name) }
                }
        ): List<T> {
            val list = mutableListOf<T>()
            var page = 1

            do {
                val response = pageRetrievalFun.invoke(page)
                list.addAll(response.getContents())

                logger.log(Log.LOW) { "Retrieved ${list.size}/${response.paging.total} $name (page ${response.paging.page}/${response.paging.pages})" }
                page = response.getNextPage() ?: break
            } while (true)


            val immutableList = list.toImmutableList()
            postRetrievalFun.invoke(immutableList)
            return immutableList
        }

        // Handle cursor responses that return maps
        // In the future we'll need to handle responses that return lists
        // TODO implement this for a function and test it
        internal fun <T, R> retrieveCursorResponseAsMap(
                name: String,
                retrievalFun: (String?) -> CursorResponse<R>,
                mappingFun: (Map<T, R>, List<R>) -> Unit
        ): Map<T, R> {
            val map = mutableMapOf<T, R>()
            var cursor: String? = null

            do {
                val response = retrievalFun.invoke(cursor)
                val contents = response.getContents()
                mappingFun.invoke(map, contents)
                logger.debug { "Retrieved ${contents.size} $name" }

                if (!response.moreEntries()) {
                    break
                }
                cursor = response.nextCursor()
            } while (true)

            return map
        }
    }
}