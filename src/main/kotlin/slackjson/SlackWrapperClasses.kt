package slackjson

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserListResponse(
        val members: List<User>
) : CursorResponse()

@JsonClass(generateAdapter = true)
data class FileListResponse(
        val files: List<ParsedFile>
) : PaginatedResponse()

@JsonClass(generateAdapter = true)
data class FileResponse (
        val file: ParsedFile
) : SlackResponse()