package slackjson

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** conversations.list **/
@JsonClass(generateAdapter = true)
data class ConversationListResponse(
        @field:Json(name = "channels")
        override val contents: List<Conversation>
) : CursorResponse<Conversation>()

/** files.info **/
@JsonClass(generateAdapter = true)
data class OldFileResponse (
        val file: ParsedFile
) : SlackResponse()

/** files.info **/
@JsonClass(generateAdapter = true)
data class FileResponse (
        @field:Json(name = "file")
        override val contents: ParsedFile?
) : SlackSimpleResponse<ParsedFile>()

/** files.list **/
@JsonClass(generateAdapter = true)
data class FileListResponse(
        @Json(name = "files")
        override val contents: List<ParsedFile>
) : PaginatedResponse<ParsedFile>()

/** users.list **/
@JsonClass(generateAdapter = true)
data class UserListResponse(
        @Json(name = "members")
        override val contents: List<User>
) : CursorResponse<User>()