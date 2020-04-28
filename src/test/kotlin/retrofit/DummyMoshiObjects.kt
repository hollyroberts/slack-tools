package retrofit

import com.squareup.moshi.JsonClass
import slackjson.PaginatedResponse

@Suppress("MemberVisibilityCanBePrivate")
@JsonClass(generateAdapter = true)
class BasicPaginatedResponse(val list: List<String>) : PaginatedResponse<String>() {
    override fun getContents(): List<String> {
        return list
    }
}