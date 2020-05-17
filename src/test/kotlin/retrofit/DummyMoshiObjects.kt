package retrofit

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import slackjson.CursorResponse
import slackjson.PaginatedResponse
import slackjson.SlackSimpleResponse

@JsonClass(generateAdapter = true)
data class StringListResponse (
        @field:Json(name = "list")
        override val contents: List<String>?
) : SlackSimpleResponse<List<String>>()

@JsonClass(generateAdapter = true)
class BasicPaginatedResponse(
        @Json(name = "list")
        override val contents: List<String>
) : PaginatedResponse<String>()

@JsonClass(generateAdapter = true)
class BasicCursorResponse(
        @Json(name = "test")
        override val contents: List<String>
) : CursorResponse<String>()