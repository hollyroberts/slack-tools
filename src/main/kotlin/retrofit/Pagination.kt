package retrofit

import okhttp3.internal.toImmutableList
import org.apache.logging.log4j.kotlin.Logging
import slackjson.CursorResponse
import slackjson.PaginatedResponse
import utils.Log

object Pagination : Logging {
    internal fun <T> retrievePaginatedList(
            name: String,
            pageRetrievalFun: (Int) -> PaginatedResponse<T>
    ): List<T> {
        val list = mutableListOf<T>()
        var page = 1

        do {
            val response = pageRetrievalFun.invoke(page)
            list.addAll(response.contents)

            logger.log(Log.LOW) { "Retrieved ${list.size}/${response.paging.total} $name (page ${response.paging.page}/${response.paging.pages})" }
            page = response.getNextPage() ?: break
        } while (true)

        return list.toImmutableList()
    }

    // Handle cursor responses that return maps
    // In the future we'll need to handle responses that return lists
    internal fun <T, R> retrieveCursorResponseAsMap(
            name: String,
            pageRetrievalFun: (String?) -> CursorResponse<R>,
            mappingFun: (R) -> T
    ): Map<T, R> {
        val map = mutableMapOf<T, R>()
        var cursor: String? = null

        do {
            val response = pageRetrievalFun.invoke(cursor)
            val contents = response.contents

            contents.forEach {
                val key = mappingFun.invoke(it)
                if (map.contains(key)) {
                    throw IllegalStateException("Map for cursor response already contains value $key")
                }
                map[key] = it
            }
            logger.debug { "Retrieved ${contents.size} $name" }

            if (!response.moreEntries()) {
                break
            }
            cursor = response.nextCursor()
        } while (true)

        logger.log(Log.HIGH) { "Retrieved %,d %s".format(map.size, name) }
        return map
    }
}