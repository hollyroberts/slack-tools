package retrofit

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
        val list: List<String>
) : SlackSimpleResponse<List<String>>() {
    override fun getContents() = list
}