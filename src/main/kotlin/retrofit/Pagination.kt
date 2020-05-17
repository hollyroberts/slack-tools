package retrofit

import okhttp3.internal.toImmutableList
import org.apache.logging.log4j.kotlin.Logging
import slackjson.CursorResponse
import slackjson.PaginatedResponse
import utils.Log

object Pagination : Logging {
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
                list.addAll(response.contents)

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
                val contents = response.contents
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