package slackjson

import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException

/*
Top level classes for slack json representations
 */
abstract class SlackResponse {
    var ok = false
    var warning: String? = null
    var error: String? = null

    fun verify() {
        if (ok) {
            return
        }

        var message = "Response from slack did not indicate success"
        warning?.let { message += "\n\t\tWarning message: $it" }
        error?.let { message += "\n\t\tError message: $it" }
        throw JsonDataException(message)
    }
}

abstract class SlackSimpleResponse<T> : SlackResponse() {
    abstract val contents: T?
}

abstract class CursorResponse<T> : SlackResponse() {
    @Suppress("PropertyName")
    lateinit var response_metadata: Cursor

    @JsonClass(generateAdapter = true)
    data class Cursor(val next_cursor: String?)

    fun moreEntries() = !nextCursor().isNullOrEmpty()

    /**
     * Returns the next cursor if it exists
     * If moreEntries is true then guaranteed to be non null
     */
    fun nextCursor() = response_metadata.next_cursor

    abstract fun getContents(): List<T>
}

abstract class PaginatedResponse<T> : SlackResponse() {
    lateinit var paging: Page

    /**
     * Looks at the paging response metadata and update the params given
     * @param params Parameters passed to GET method
     * @return False if there are no more results to be retrieved
     */
    @Deprecated("To be removed soon TM")
    fun updatePageParams(params: MutableMap<String, String>) : Boolean {
        if (paging.page >= paging.pages) {
            return false
        }

        params["page"] = (paging.page + 1).toString()
        return true
    }

    fun getNextPage(): Int? {
        if (paging.page >= paging.pages) {
            return null
        }

        return paging.page + 1
    }

    abstract fun getContents(): List<T>
}

@JsonClass(generateAdapter = true)
data class Page(
        val count: Int,
        val total: Int,
        val page: Int,
        val pages: Int
)