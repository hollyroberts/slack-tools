package retrofit

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import slackjson.PaginatedResponse
import slackjson.SlackSimpleResponse

@Suppress("MemberVisibilityCanBePrivate")
@JsonClass(generateAdapter = true)
class BasicPaginatedResponse(val list: List<String>) : PaginatedResponse<String>() {
    override fun getContents(): List<String> {
        return list
    }
}

@JsonClass(generateAdapter = true)
data class StringListResponse (
        @field:Json(name = "list")
        override val contents: List<String>?
) : SlackSimpleResponse<List<String>>()