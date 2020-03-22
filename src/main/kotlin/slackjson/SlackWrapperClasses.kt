package slackjson

import com.squareup.moshi.JsonClass

/** conversations.list **/
@JsonClass(generateAdapter = true)
data class ConversationListResponse(
        val channels: List<Conversation>
) : CursorResponse()

/** files.info **/
@JsonClass(generateAdapter = true)
data class FileResponse (
        val file: ParsedFile
) : SlackResponse()

/** files.list **/
@JsonClass(generateAdapter = true)
data class FileListResponse(
        val files: List<ParsedFile>
) : PaginatedResponse()

/** users.list **/
@JsonClass(generateAdapter = true)
data class UserListResponse(
        val members: List<User>
) : CursorResponse()