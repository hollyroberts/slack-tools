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
    //@Json(name = "page.count")
    //val count: Int? = null
}

@JsonClass(generateAdapter = true)
data class Page(
        val count: Int,
        val total: Int,
        val page: Int,
        val pages: Int
)