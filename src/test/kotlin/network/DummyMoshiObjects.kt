package network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import json.CursorResponse
import json.PaginatedResponse
import json.SlackSimpleResponse
import network.TestCursorMapResponse.CursorResponseContents

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
class TestCursorListResponse(
        @Json(name = "test")
        override val contents: List<String?>
) : CursorResponse<String?>()

@JsonClass(generateAdapter = true)
class TestCursorMapResponse(
        @Json(name = "test")
        override val contents: List<CursorResponseContents>
) : CursorResponse<CursorResponseContents>() {
        @JsonClass(generateAdapter = true)
        data class CursorResponseContents(
                val a: String,
                val b: String
        )
}