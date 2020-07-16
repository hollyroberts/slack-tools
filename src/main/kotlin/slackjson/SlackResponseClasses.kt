package slackjson

import com.squareup.moshi.Json
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
        if (warning == null && error == null) {
            message += ". No information about the failure was provided."
        }
        throw JsonDataException(message)
    }
}

abstract class SlackSimpleResponse<T> : SlackResponse() {
    abstract val contents: T?
}

abstract class CursorResponse<T> : SlackResponse() {
    abstract val contents: List<T>

    @field:Json(name = "response_metadata")
    var metadata: Cursor? = null

    @JsonClass(generateAdapter = true)
    data class Cursor(
            @Json(name = "next_cursor")
            val nextCursor: String?
    )

    fun moreEntries() = !nextCursor().isNullOrBlank()

    /**
     * Returns the next cursor if it exists
     * If moreEntries is true then guaranteed to be non null
     */
    fun nextCursor() = metadata?.nextCursor
}

abstract class PaginatedResponse<T> : SlackResponse() {
    abstract val contents: List<T>
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
}

@JsonClass(generateAdapter = true)
data class Page(
        val count: Int,
        val total: Int,
        val page: Int,
        val pages: Int
)