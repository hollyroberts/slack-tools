package network

import json.CursorResponse
import json.PaginatedResponse
import okhttp3.internal.toImmutableList
import org.apache.logging.log4j.kotlin.Logging
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

    // If we need to support sequence filtering + addAll then split this into base generic function and 2 separate impls for it
    internal fun <T, R> retrieveCursorResponseAsList(
            name: String,
            pageRetrievalFun: (String?) -> CursorResponse<R>,
            appenderFun: (List<R>) -> Sequence<T>
    ): MutableList<T> {
        val list = mutableListOf<T>()
        var cursor: String? = null

        do {
            val response = pageRetrievalFun.invoke(cursor)
            val contents = response.contents
            list.addAll(appenderFun.invoke(contents))
            logger.debug("Retrieved ${list.size} $name")

            if (!response.moreEntries()) {
                break
            }
            cursor = response.nextCursor()
        } while (true)

        return list
    }

    // Handle cursor responses that return maps
    internal fun <T, R> retrieveCursorResponseAsMap(
            name: String,
            pageRetrievalFun: (String?) -> CursorResponse<R>,
            mappingFun: (R) -> T
    ): MutableMap<T, R> {
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
            logger.debug { "Retrieved ${map.size} $name" }

            if (!response.moreEntries()) {
                break
            }
            cursor = response.nextCursor()
        } while (true)

        return map
    }
}