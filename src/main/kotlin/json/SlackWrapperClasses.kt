package json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import json.slack.file.ParsedFile
import json.slack.message.BaseMessage
import json.slack.metadata.Conversation
import json.slack.metadata.User

/** conversations.list **/
@JsonClass(generateAdapter = true)
data class ConversationListResponse(
        @field:Json(name = "channels")
        override val contents: List<Conversation>
) : CursorResponse<Conversation>()

/** conversations.history **/
@JsonClass(generateAdapter = true)
data class ConversationHistoryResponse(
        @Json(name = "messages")
        override val contents: List<BaseMessage?>
) : CursorResponse<BaseMessage?>()

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