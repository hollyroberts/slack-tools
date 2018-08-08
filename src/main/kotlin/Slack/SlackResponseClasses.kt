package Slack

import com.squareup.moshi.JsonClass

/*
Top level classes for slack json representations
 */

@JsonClass(generateAdapter = true)
open class SlackResponse {
    var ok = false
    var warning: String? = null
    var error: String? = null
}

@JsonClass(generateAdapter = true)
open class CursorResponse : SlackResponse() {
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
}

@JsonClass(generateAdapter = true)
open class PaginatedResponse : SlackResponse() {
    lateinit var paging: Page

    /**
     * Looks at the paging response metadata and update the params given
     * @param params Parameters passed to GET method
     * @return False if there are no more results to be retrieved
     */
    fun updatePageParams(params: MutableMap<String, String>) : Boolean {
        if (paging.page >= paging.pages) {
            return false
        }

        params["page"] = (paging.page + 1).toString()
        return true
    }
}

@JsonClass(generateAdapter = true)
data class Page(
        val count: Int,
        val total: Int,
        val page: Int,
        val pages: Int
)