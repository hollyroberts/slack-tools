package slackjson

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** conversations.list **/
@JsonClass(generateAdapter = true)
data class ConversationListResponse(
        val channels: List<Conversation>
) : CursorResponse<Conversation>() {
    override fun getContents() = channels
}

/** files.info **/
@JsonClass(generateAdapter = true)
data class OldFileResponse (
        val file: ParsedFile
) : SlackResponse()

/** files.info **/
@JsonClass(generateAdapter = true)
data class FileResponse (
        @Json(name = "file")
        override val contents: ParsedFile?
) : SlackSimpleResponse<ParsedFile>()

/** files.list **/
@JsonClass(generateAdapter = true)
data class FileListResponse(
        val files: List<ParsedFile>
) : PaginatedResponse<ParsedFile>() {
    override fun getContents(): List<ParsedFile> = files
}

/** users.list **/
@JsonClass(generateAdapter = true)
data class UserListResponse(
        val members: List<User>
) : CursorResponse<User>() {
    override fun getContents() = members
}