package retrofit

import okhttp3.internal.toImmutableList
import retrofit.SlackTier.TIER_3
import retrofit2.http.GET
import retrofit2.http.Query
import slackjson.CursorResponse
import slackjson.FileListResponse
import slackjson.PaginatedResponse
import utils.Log

interface SlackApi {
    @GET("files.list?count=100")
    @Tier(TIER_3)
    fun listFilesPage(
            @Query("page") page: Int = 1,
            @Query("ts_from") startTime: Long? = null,
            @Query("ts_to") endTime: Long? = null,
            @Query("channel") channel: String? = null,
            @Query("user") user: String? = null
    ): SlackResult<FileListResponse>

    @JvmDefault
    fun listFiles(
            startTime: Long? = null,
            endTime: Long? = null,
            channel: String? = null,
            user: String? = null
    ) = retrievePaginatedList("files") {
        listFilesPage(
                page = it,
                startTime = startTime,
                endTime = endTime,
                channel = channel,
                user = user
        ).result
    }

    companion object {
        internal fun <T> retrievePaginatedList(name: String, pageRetrievalFun: (Int) -> PaginatedResponse<T>): List<T> {
            val list = mutableListOf<T>()
            var page = 1

            do {
                val response = pageRetrievalFun.invoke(page)

                Log.medium("Retrieved ${list.size}/${response.paging.total} $name (page ${response.paging.page}/${response.paging.pages})")
                list.addAll(response.getContents())
                page = response.getNextPage() ?: break
            } while (true)

            return list.toImmutableList()
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
                Log.debugHigh("Retrieved ${contents.size} $name")

                if (!response.moreEntries()) {
                    break
                }
                cursor = response.nextCursor()
            } while (true)

            return map
        }
    }
}